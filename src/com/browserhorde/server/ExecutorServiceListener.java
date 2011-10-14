package com.browserhorde.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.browserhorde.server.util.ParamUtils;

public class ExecutorServiceListener implements ServletContextListener {
	public static final String EXECUTOR_NAME = ExecutorService.class.getName();

	private final Logger log = Logger.getLogger(getClass());

    @Override
	public void contextInitialized(ServletContextEvent evt) {
    	ServletContext ctx = evt.getServletContext();

    	Integer corePoolSize = ParamUtils.asInteger(ctx.getInitParameter(ServletInitOptions.EXECUTOR_CORE_POOL_SIZE), 1);
    	Integer maxPoolSize = ParamUtils.asInteger(ctx.getInitParameter(ServletInitOptions.EXECUTOR_MAX_POOL_SIZE), 4);
    	Integer keepAliveTimeout = ParamUtils.asInteger(ctx.getInitParameter(ServletInitOptions.EXECUTOR_KEEP_ALIVE_TIMEOUT), 300);
    	Boolean allowCoreThreadTimeout = ParamUtils.asBoolean(ctx.getInitParameter(ServletInitOptions.EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT), false);
    	
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

		ctx.setAttribute(EXECUTOR_NAME, executor);
    }
    @Override
	public void contextDestroyed(ServletContextEvent evt) {
    	ServletContext ctx = evt.getServletContext();
    	ExecutorService executor = (ExecutorService)ctx.getAttribute(EXECUTOR_NAME);
    	ctx.setAttribute(EXECUTOR_NAME, null);

    	try {
    		executor.shutdown();
			if(!executor.awaitTermination(30, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch(InterruptedException ex) {
			log.error("Clean shutdown failed", ex);
		}
    }
}
