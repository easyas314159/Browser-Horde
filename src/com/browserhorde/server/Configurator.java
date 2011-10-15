package com.browserhorde.server;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.browserhorde.server.gson.DateHandler;
import com.browserhorde.server.gson.GsonUtils;
import com.browserhorde.server.gson.JsonElementHandler;
import com.browserhorde.server.gson.URIHandler;
import com.browserhorde.server.gson.URLHandler;
import com.browserhorde.server.queue.GzipThread;
import com.browserhorde.server.queue.MessageHandler;
import com.browserhorde.server.queue.MinifyThread;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonElement;
import com.google.inject.Injector;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	private final String THREAD_MINIFY = MinifyThread.class.getName();
	private final String THREAD_GZIP = GzipThread.class.getName();

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		Injector injector = (Injector)context.getAttribute(Injector.class.getName());

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

			Thread threadMinify = injector.getInstance(MinifyThread.class);
			Thread threadGzip = injector.getInstance(GzipThread.class);

			threadMinify.start();
			threadGzip.start();

			context.setAttribute(THREAD_MINIFY, threadMinify);
			context.setAttribute(THREAD_GZIP, threadGzip);
		}
		catch(Throwable t) {
			log.fatal("BOOM!", t);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		ServletContext context = event.getServletContext();

		MessageHandler threadGzip = (MessageHandler)context.getAttribute(THREAD_GZIP);
		MessageHandler threadMinify = (MessageHandler)context.getAttribute(THREAD_MINIFY);

		threadGzip.shutdown();
		threadMinify.shutdown();

		context.setAttribute(THREAD_GZIP, null);
		context.setAttribute(THREAD_MINIFY, null);
	}
}
