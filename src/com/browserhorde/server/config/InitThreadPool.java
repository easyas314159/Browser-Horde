package com.browserhorde.server.config;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import com.browserhorde.server.Configurator;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;

public class InitThreadPool extends ConfigCommand {
	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		Runtime rt = Runtime.getRuntime();
		ServletContext context = ctx.getServletContext();

		// TODO: Externalize these options into context config
		int core_pool_size = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_CORE_POOL_SIZE), 1);
		int max_pool_size = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_MAX_POOL_SIZE), rt.availableProcessors() << 1);
		int keep_alive_timeout = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_CORE_THREAD_TIMEOUT), 300);
		boolean allow_core_thread_timeout = ParamUtils.asBoolean(context.getInitParameter(ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT), true);

		if(max_pool_size < core_pool_size) {
			core_pool_size = max_pool_size;
			log.warn(String.format("%s may not be larger than %s", ServletInitOptions.EXECUTOR_CORE_POOL_SIZE, ServletInitOptions.EXECUTOR_MAX_POOL_SIZE));
		}
		if(keep_alive_timeout < 0) {
			keep_alive_timeout = 0;
			log.warn(String.format("%s may not be less than 0", ServletInitOptions.EXECUTOR_CORE_THREAD_TIMEOUT));
		}
		if(keep_alive_timeout == 0 && allow_core_thread_timeout) {
			allow_core_thread_timeout = false;
			log.warn(String.format("%s must be greater than 0 when setting %s to true", ServletInitOptions.EXECUTOR_CORE_THREAD_TIMEOUT, ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT));
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