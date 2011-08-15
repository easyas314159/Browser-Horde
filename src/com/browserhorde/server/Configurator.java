package com.browserhorde.server;

import java.util.Date;
import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.browserhorde.server.api.json.WorkorderResponse;
import com.browserhorde.server.api.json.WorkorderResponseSerializer;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.json.JobSerializer;
import com.browserhorde.server.entity.json.ScriptSerializer;
import com.browserhorde.server.util.DateAdapter;
import com.browserhorde.server.util.GsonUtils;

public class Configurator implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		GsonUtils.getGsonBuilder()
			.setPrettyPrinting()
			.registerTypeAdapter(Date.class, new DateAdapter())
			.registerTypeHierarchyAdapter(Date.class, new DateAdapter())
			.registerTypeHierarchyAdapter(Job.class, new JobSerializer())
			.registerTypeHierarchyAdapter(Script.class, new ScriptSerializer())
			.registerTypeHierarchyAdapter(WorkorderResponse.class, new WorkorderResponseSerializer())
			;
	}
	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
