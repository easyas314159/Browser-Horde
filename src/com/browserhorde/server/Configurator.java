package com.browserhorde.server;

import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.browserhorde.server.util.DateHandler;
import com.browserhorde.server.util.GsonUtils;
import com.browserhorde.server.util.JsonElementHandler;
import com.browserhorde.server.util.URIHandler;
import com.browserhorde.server.util.URLHandler;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonElement;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	public void contextInitialized(ServletContextEvent event) {
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	
			// TODO: This need to be tightened up security wise
			GsonUtils.getGsonBuilder()
				.setPrettyPrinting()
				.excludeFieldsWithoutExposeAnnotation()
				.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
				.registerTypeAdapter(Date.class, new DateHandler())
				.registerTypeHierarchyAdapter(JsonElement.class, new JsonElementHandler())
				.registerTypeHierarchyAdapter(URI.class, new URIHandler())
				.registerTypeHierarchyAdapter(URL.class, new URLHandler());
				;
		}
		catch(Throwable t) {
			log.fatal("BOOM!", t);
		}
	}
	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
