package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ExecutorServiceProvider implements Provider<ExecutorService> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private ServletContext context;

	@Override
	public ExecutorService get() {
		Runtime rt = Runtime.getRuntime();

		int core_pool_size = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_CORE_POOL_SIZE), 1);
		int max_pool_size = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_MAX_POOL_SIZE), rt.availableProcessors() << 1);
		int keep_alive_timeout = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.EXECUTOR_CORE_THREAD_TIMEOUT), 300);
		boolean allow_core_thread_timeout = ParamUtils.asBoolean(context.getInitParameter(ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT), true);

		if(max_pool_size < 1) {
			max_pool_size = 1;
		}
		if(core_pool_size < 1) {
			core_pool_size = 1;
		}
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

		return executor;
	}
}
