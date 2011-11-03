package com.browserhorde.server;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.codec.digest.DigestUtils;

import com.browserhorde.server.api.ApiHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XMachineIdFilter extends HttpFilter implements Filter {
	private static final Pattern uuidPattern = Pattern.compile(
			"\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}"
		);

	private static final String NS_MACHINE_ID = DigestUtils.md5Hex("machine_id");

	@Inject private MemcachedClient memcached;

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		// TODO: Maybe tie this into auth so we can track the number of machines each user has
		final String checkHeaders[] = new String[]{
			ApiHeaders.X_HORDE_MACHINE_ID,
			HttpHeaders.IF_NONE_MATCH,
			HttpHeaders.IF_MODIFIED_SINCE
		};

		// FIXME: This needs some fixing
		String machineId = null;
		boolean userIsNice = true;
		Set<String> machineIds = new HashSet<String>();
		for(String headerName : checkHeaders) {
			Enumeration<String> headerValues = req.getHeaders(headerName);
			while(headerValues.hasMoreElements()) {
				String headerValue = headerValues.nextElement();
				Matcher matcher = uuidPattern.matcher(headerValue); 
				if(matcher.matches()) {
					machineIds.add(headerValue.toLowerCase());
				}
			}
			if(machineIds.size() == 1) {
				machineId = machineIds.iterator().next();
				break;
			}
			machineIds.clear();
		}
		if(machineId == null) {
			machineId = UUID.randomUUID().toString();
		}

		req = new ModifyHeaderWrapper(req)
			.withHeader(ApiHeaders.X_HORDE_MACHINE_ID, machineId);

		rsp.setHeader(ApiHeaders.X_HORDE_MACHINE_ID, machineId);
		rsp.setHeader(HttpHeaders.ETAG, machineId);
		rsp.setHeader(HttpHeaders.LAST_MODIFIED, machineId);

		chain.doFilter(req, rsp);
	}
}
