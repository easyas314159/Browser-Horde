package com.browserhorde.server.cors;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.HttpFilter;
import com.browserhorde.server.api.ApiStatus;

public class PreflightHijackFilter extends HttpFilter {

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		if(StringUtils.equals((String)req.getAttribute("cors.requestType"), "preflight")) {
			rsp.setStatus(ApiStatus.OK.getStatusCode());
		}
		else {
			chain.doFilter(req, rsp);
		}
	}

}
