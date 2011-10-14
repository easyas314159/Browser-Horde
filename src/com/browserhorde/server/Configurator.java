package com.browserhorde.server;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.browserhorde.server.gson.DateHandler;
import com.browserhorde.server.gson.GsonUtils;
import com.browserhorde.server.gson.JsonElementHandler;
import com.browserhorde.server.gson.URIHandler;
import com.browserhorde.server.gson.URLHandler;
import com.browserhorde.server.queue.GzipProcessor;
import com.browserhorde.server.queue.MinifyProcessor;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonElement;
import com.google.inject.Injector;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	private final String MINIFY_FUTURE = MinifyProcessor.class.getName();
	private final String GZIP_FUTURE = GzipProcessor.class.getName();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		Injector injector = (Injector)context.getAttribute(Injector.class.getName());
		ScheduledExecutorService executorService =
				(ScheduledExecutorService)context.getAttribute(ExecutorServiceListener.EXECUTOR_NAME);

		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

			// TODO: This needs to be tightened up security wise
			GsonUtils.getGsonBuilder()
				.setPrettyPrinting()
				.excludeFieldsWithoutExposeAnnotation()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.registerTypeAdapter(Date.class, new DateHandler())
				.registerTypeHierarchyAdapter(JsonElement.class, new JsonElementHandler())
				.registerTypeHierarchyAdapter(URI.class, new URIHandler())
				.registerTypeHierarchyAdapter(URL.class, new URLHandler());
				;

			Runnable minifyProcessor = injector.getInstance(MinifyProcessor.class);
			Runnable gzipProcessor = injector.getInstance(GzipProcessor.class);

			ScheduledFuture<?> minifyFuture = executorService.scheduleAtFixedRate(minifyProcessor, 0, 15, TimeUnit.SECONDS);
			ScheduledFuture<?> gzipFuture = executorService.scheduleAtFixedRate(gzipProcessor, 0, 15, TimeUnit.SECONDS);

			context.setAttribute(MINIFY_FUTURE, minifyFuture);
			context.setAttribute(GZIP_FUTURE, gzipFuture);
		}
		catch(Throwable t) {
			log.fatal("BOOM!", t);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();

		ScheduledFuture<?> gzipFuture = (ScheduledFuture<Void>)context.getAttribute(GZIP_FUTURE);
		ScheduledFuture<?> minifyFuture = (ScheduledFuture<Void>)context.getAttribute(MINIFY_FUTURE);

		context.setAttribute(GZIP_FUTURE, null);
		context.setAttribute(MINIFY_FUTURE, null);

		gzipFuture.cancel(false);
		minifyFuture.cancel(false);
	}
}
