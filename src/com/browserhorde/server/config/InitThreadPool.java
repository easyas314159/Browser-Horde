package com.browserhorde.server.config;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import com.browserhorde.server.Configurator;

public class InitThreadPool extends ConfigCommand {
	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		Runtime rt = Runtime.getRuntime();
		ServletContext context = ctx.getServletContext();

		// TODO: Externalize these options into context config
		int core_pool_size = 1;
		int max_pool_size = rt.availableProcessors() << 1;
		int keep_alive_timeout = 300;
		boolean allow_core_thread_timeout = false;

		if(max_pool_size < core_pool_size) {
			core_pool_size = max_pool_size;
		}
		if(keep_alive_timeout == 0 && allow_core_thread_timeout) {
			allow_core_thread_timeout = false;
		}

		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(core_pool_size);

		executor.setKeepAliveTime(keep_alive_timeout, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(allow_core_thread_timeout);
		executor.setMaximumPoolSize(max_pool_size);

		// TODO: Make this gracefully log and rejections
		//executor.setRejectedExecutionHandler(handler);

		// TODO: Allow custom thread factory configuration
		//executor.setThreadFactory(threadFactory);

		context.setAttribute(Configurator.EXECUTOR_SERVICE, executor);

		return false;
	}		
}