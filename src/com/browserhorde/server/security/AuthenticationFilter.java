package com.browserhorde.server.security;

import java.io.IOException;
import java.security.Principal;
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
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.HttpFilter;
import com.browserhorde.server.entity.User;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationFilter extends HttpFilter {
	private final Logger log = Logger.getLogger(getClass());

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
		String authHeader = StringUtils.trimToNull(req.getHeader("Authorization"));
		if(authHeader == null) {
			// TODO: Check for oauth headers
		}
		else {
			String header[] = authHeader.split("\\s+", 2);
			if(header.length == 2 && StringUtils.equalsIgnoreCase("Basic", header[0])) {
				header = new String(Base64.decodeBase64(header[1])).split(":", 2);
				if(header.length == 2) {
					user = getUser(header[0], header[1]);
				}
				else {
					rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			}
			else {
				rsp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return;
			}
		}

		if(user != null) {
			req = new AuthenticatedRequestWrapper(req, user);
		}
		rsp = new AuthenticatedResponseWrapper(rsp);

		chain.doFilter(req, rsp);
	}

	private User getUser(String email, String password) {
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		// TODO: Maybe the objects name should be the email to speed this up
		Query query = entityManager.createQuery(
				"select * from " + User.class.getName()
				+ " where email=:email"
			);
		query.setParameter("email", email);

		List<User> users = query.getResultList();
		entityManager.close();

		if(users.size() == 1) {
			User user = users.get(0);
			if(user.matchesPassword(password)) {
				return users.get(0);
			}
		}
		else if(users.size() > 1) {
			log.error(String.format("Duplicate e-mail \'%s\' in the user table!", email));
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
			// TODO: This may be more suitable in a listener or something

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

	// TODO: This should do some filtering to convert certain responses to 404
	private final class AuthenticatedResponseWrapper extends HttpServletResponseWrapper {
		public AuthenticatedResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void setStatus(int sc) {
			setStatus(sc, null);
		}
		@Override
		public void setStatus(int sc, String sm) {
			super.setStatus(transformStatus(sc), sm);
		}

		@Override
		public void sendError(int sc) throws IOException {
			sendError(sc, null);
		}
		@Override
		public void sendError(int sc, String msg) throws IOException {
			super.sendError(transformStatus(sc), msg);
		}

		// TODO: This should transform the message as well
		private int transformStatus(int sc) {
			if(sc == HttpServletResponse.SC_FORBIDDEN) {
				sc = HttpServletResponse.SC_NOT_FOUND;
			}
			return sc;
		}
	}
}
