package com.browserhorde.server;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.inject.Singleton;

@Singleton
public class XRuntime extends HttpFilter {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public void init(FilterConfig config) throws ServletException {
	}
	@Override
	public void destroy() {
	}
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		long millis = System.currentTimeMillis();
		chain.doFilter(req, rsp);
		millis = System.currentTimeMillis() - millis;

		double elapsed = 1.0E-3 * millis;
		rsp.setHeader("X-Runtime", Double.toString(elapsed));
		log.debug(String.format("%f", elapsed));
	}
}
