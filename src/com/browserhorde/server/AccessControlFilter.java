package com.browserhorde.server;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Handles cross-origin resource sharing as per http://www.w3.org/TR/cors/
 * 
 * @author kloney
 *
 */
public class AccessControlFilter extends HttpFilter {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		String origin = req.getHeader("Origin");
		if(origin != null) {
			rsp.setHeader("Access-Control-Allow-Origin", "*");
			rsp.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
		}
		chain.doFilter(req, rsp);
	}	
}
