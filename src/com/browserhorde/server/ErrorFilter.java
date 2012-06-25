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
public class ErrorFilter extends HttpFilter {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(req, rsp);
		}
		catch(IOException ex) {
			throw ex;
		}
		catch(ServletException ex) {
			throw ex;
		}
		catch(Throwable t) {
			log.fatal("ZOMGWTFBBQ!", t);
		}
	}

}
