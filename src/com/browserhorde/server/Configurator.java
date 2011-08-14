package com.browserhorde.server;

import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.json.JobSerializer;
import com.browserhorde.server.entity.json.ScriptSerializer;
import com.browserhorde.server.util.GsonUtils;

public class Configurator implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		GsonUtils.getGsonBuilder()
			.setPrettyPrinting()
			.registerTypeHierarchyAdapter(Job.class, new JobSerializer())
			.registerTypeHierarchyAdapter(ScriptSerializer.class, new ScriptSerializer())
			;
	}
	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
