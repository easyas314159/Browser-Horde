package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.browserhorde.server.ExecutorServiceListener;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutorServiceProvider implements Provider<ExecutorService> {
	@Inject private ServletContext ctx;

	@Override
	public ExecutorService get() {
		ExecutorService executor = (ExecutorService)ctx.getAttribute(ExecutorServiceListener.ATTRIBUTE);
		return executor;
	}
}
