package com.browserhorde.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class HttpFilter implements Filter {
	@Override
	public void doFilter(ServletRequest req, ServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		if(req instanceof HttpServletRequest && rsp instanceof HttpServletResponse) {
			doFilter((HttpServletRequest)req, (HttpServletResponse)rsp, chain);
		}
		else {
			chain.doFilter(req, rsp);
		}
	}

	public abstract void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException;
}
