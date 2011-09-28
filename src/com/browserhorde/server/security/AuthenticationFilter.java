package com.browserhorde.server.security;

import java.io.IOException;
import java.net.InetAddress;
import java.security.Principal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.HttpFilter;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.gson.GsonTranscoder;
import com.browserhorde.server.gson.StringTranscoder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationFilter extends HttpFilter {
	private static final String NS_USERID_BY_EMAIL = DigestUtils.md5Hex("userid_by_email");
	private static final String NS_BASIC = DigestUtils.md5Hex("auth_basic");
	private static final String NS_OAUTH = DigestUtils.md5Hex("auth_oauth");

	private final Logger log = Logger.getLogger(getClass());

	@Inject private MemcachedClient memcached;
	@Inject private EntityManagerFactory entityManagerFactory;

	@Override
	public void destroy() {
	}
	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		// TODO: Is there anything we can do about caching stuff here to speed things up?
		// TODO: Need to have some kind of throttling in here to prevent brute force password attacks

		User user = null;
		InetAddress ip = InetAddress.getByName(req.getRemoteAddr());
		Enumeration<String> authHeaders = req.getHeaders(HttpHeaders.AUTHORIZATION);
		while(user == null && authHeaders.hasMoreElements()) {
			String authHeader = authHeaders.nextElement();

			authHeader = StringUtils.trimToNull(authHeader);
			if(authHeader == null) {
				continue;
			}

			user = getUserFromHeader(ip, authHeader);
		}

		chain.doFilter(
				new AuthenticatedRequestWrapper(req, user),
				rsp
			);
	}

	private User getUserFromHeader(InetAddress ip, String authHeader) {
		String header[] = authHeader.split("\\s+", 2);

		if(header.length == 2 && StringUtils.equalsIgnoreCase("Basic", header[0])) {
			header = new String(Base64.decodeBase64(header[1])).split(":", 2);
			if(header.length == 2) {
				String username = header[0];
				String password = header[1];

				return getUser(ip, username, password);
			}
		}
		return null;
	}

	private User getUser(InetAddress ip, String email, String password) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		User user = null;
		String userIdKey = NS_USERID_BY_EMAIL + DigestUtils.md5Hex(email);
		String userId = memcached.get(userIdKey, new StringTranscoder());

		if(userId == null) {
			Query query = entityManager.createQuery(
					"select * from " + User.class.getName()
					+ " where email=:email"
				);
			query.setParameter("email", email);

			List<User> users = query.getResultList();
			if(users.size() == 1) {
				user = users.get(0);
				memcached.set(userIdKey, 0, user.getId(), new StringTranscoder());
			}
			else if(users.size() > 1) {
				log.fatal(String.format("Duplicate e-mail \'%s\' in the user table!", email));
			}
		}
		else {
			user = entityManager.find(User.class, userId);
		}

		if(user == null) {
			return null;
		}

		GsonTranscoder<byte[]> tc = new GsonTranscoder<byte[]>(byte[].class);
		byte[] authHash = DigestUtils.sha256(
				ArrayUtils.addAll(
						ip.getAddress(),
						password.getBytes()
					)
			);
	
		String key = NS_BASIC + DigestUtils.md5Hex(ArrayUtils.addAll(ip.getAddress(), email.getBytes()));
		byte[] cachedHash = memcached.get(key, new GsonTranscoder<byte[]>(byte[].class));
	 
		if(cachedHash == null) {
			if(user.matchesPassword(password)) {
				// BCrypt is an expensive operation so lets cache the results on the first successful auth from the ip address
				memcached.set(key, 600, authHash, tc);
				return user;
			}
		}
		else if(Arrays.equals(authHash, cachedHash)) {
			return user;
		}
		
		return null;
	}

	private final class AuthenticatedRequestWrapper extends HttpServletRequestWrapper {
		private final User user;

		public AuthenticatedRequestWrapper(HttpServletRequest request, User user) {
			super(request);

			this.user = user;
		}

		@Override
		public Principal getUserPrincipal() {
			return user;
		}

		@Override
		public boolean isUserInRole(String role) {
			// An anonymous user doesn't have any roles
			if(user == null) {
				return false;
			}

			// The most basic role is a registered user
			if(Roles.REGISTERED.equals(role)) {
				return true;
			}

			// TODO: Some user magic here so we can filter what users can do
			return super.isUserInRole(role);
		}

	}
}
