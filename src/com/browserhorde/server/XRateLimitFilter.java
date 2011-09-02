package com.browserhorde.server;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.MemcachedClient;

import com.browserhorde.server.api.ApiHttpHeaders;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XRateLimitFilter extends HttpFilter {
	@Inject private MemcachedClient memcached;

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		// TODO: Add rate limiting headers

		rsp.addIntHeader(ApiHttpHeaders.X_RATE_LIMIT, 0);
		rsp.addIntHeader(ApiHttpHeaders.X_RATE_LIMIT_REMAINING, 0);
		rsp.addDateHeader(ApiHttpHeaders.X_RATE_LIMIT_RESET, Long.MAX_VALUE);

		chain.doFilter(req, rsp);
	}
}
