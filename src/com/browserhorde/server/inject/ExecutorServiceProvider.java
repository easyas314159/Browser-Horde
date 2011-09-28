package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ExecutorServiceProvider implements Provider<ExecutorService> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject @Named(ServletInitOptions.EXECUTOR_CORE_POOL_SIZE) private Integer corePoolSize;
	@Inject @Named(ServletInitOptions.EXECUTOR_MAX_POOL_SIZE) private Integer maxPoolSize;
	@Inject @Named(ServletInitOptions.EXECUTOR_KEEP_ALIVE_TIMEOUT) private Integer keepAliveTimeout;
	@Inject @Named(ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT) private Boolean allowCoreThreadTimeout;

	@Override
	public ExecutorService get() {
		if(maxPoolSize < 1) {
			maxPoolSize = 1;
		}
		if(corePoolSize < 1) {
			corePoolSize = 1;
		}
		if(maxPoolSize < corePoolSize) {
			corePoolSize = maxPoolSize;
			log.warn(String.format("%s may not be larger than %s", ServletInitOptions.EXECUTOR_CORE_POOL_SIZE, ServletInitOptions.EXECUTOR_MAX_POOL_SIZE));
		}
		if(keepAliveTimeout < 0) {
			keepAliveTimeout = 0;
			log.warn(String.format("%s may not be less than 0", ServletInitOptions.EXECUTOR_KEEP_ALIVE_TIMEOUT));
		}
		if(keepAliveTimeout == 0 && allowCoreThreadTimeout) {
			allowCoreThreadTimeout = false;
			log.warn(String.format("%s must be greater than 0 when setting %s to true", ServletInitOptions.EXECUTOR_KEEP_ALIVE_TIMEOUT, ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT));
		}


		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize);

		executor.setKeepAliveTime(keepAliveTimeout, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(allowCoreThreadTimeout);
		executor.setMaximumPoolSize(maxPoolSize);

		// TODO: Make this gracefully log and rejections
		//executor.setRejectedExecutionHandler(handler);

		// TODO: Allow custom thread factory configuration
		//executor.setThreadFactory(threadFactory);

		return executor;
	}
}
