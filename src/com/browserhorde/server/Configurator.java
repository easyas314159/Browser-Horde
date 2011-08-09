package com.browserhorde.server;

import java.util.TimeZone;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Configurator implements ServletContextListener {
	public void contextInitialized(ServletContextEvent event) {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
