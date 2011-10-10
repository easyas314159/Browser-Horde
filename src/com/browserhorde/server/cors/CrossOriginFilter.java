package com.browserhorde.server.cors;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.browserhorde.server.HttpFilter;

public class CrossOriginFilter extends HttpFilter {
	private static final String CORS_ALLOW_GENERIC_HTTP_REQUESTS = "cors.allowGenericHttpRequests";
	private static final String CORS_ALLOW_ORIGIN = "cors.allowOrigin";
	private static final String CORS_SUPPORTED_METHODS = "cors.supportedMethods";
	private static final String CORS_SUPPORTED_HEADERS = "cors.supportedHeaders";
	private static final String CORS_EXPOSED_HEADERS = "cors.exposedHeaders";
	private static final String CORS_SUPPORTS_CREDENTIALS = "cors.supportsCredentials";
	private static final String CORS_MAX_AGE = "cors.maxAge";

	@Override
	public void init(FilterConfig config) throws ServletException {
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		
	}
}
