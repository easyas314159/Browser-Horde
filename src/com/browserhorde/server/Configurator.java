package com.browserhorde.server;

import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.browserhorde.server.util.DateAdapter;
import com.browserhorde.server.util.GsonUtils;
import com.browserhorde.server.util.JsonElementSerializer;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.JsonElement;

public class Configurator implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		// TODO: This need to be tightened up security wise
		GsonUtils.getGsonBuilder()
			.setPrettyPrinting()
			.excludeFieldsWithoutExposeAnnotation()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(Date.class, new DateAdapter())
			.registerTypeHierarchyAdapter(JsonElement.class, new JsonElementSerializer())
			;
	}
	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
