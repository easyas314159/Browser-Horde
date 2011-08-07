package com.browserhorde.server.config;

import java.util.HashMap;

import javax.servlet.ServletContext;

import org.apache.commons.chain.Context;

@SuppressWarnings("unchecked")
public class ConfigContext extends HashMap implements Context {
	private static final String KEY_SERVLET_CONTEXT = "servlet_context";

	public ConfigContext(ServletContext context) {
		put(KEY_SERVLET_CONTEXT, context);
	}
	public ServletContext getServletContext() {
		return (ServletContext)get(KEY_SERVLET_CONTEXT);
	}
}