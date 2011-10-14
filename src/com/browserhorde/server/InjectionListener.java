package com.browserhorde.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.browserhorde.server.aws.AmazonWebServicesModule;
import com.browserhorde.server.inject.CoreModule;
import com.browserhorde.server.inject.QueueModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;

public class InjectionListener implements ServletContextListener {
	public static final String INJECTOR_NAME = Injector.class.getName();

	private Logger log = Logger.getLogger(getClass());

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		try {
			ServletContext context = evt.getServletContext();
			Injector injector = getInjector(context);
			context.setAttribute(INJECTOR_NAME, injector);
		}
		catch(Throwable t) {
			log.fatal("DI Initialization Failed!", t);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		try {
			ServletContext context = evt.getServletContext();
			//Injector injector = (Injector)context.getAttribute(INJECTOR_NAME);
			context.setAttribute(INJECTOR_NAME, null);
		}
		catch(Throwable t) {
			log.fatal("DI Destruction Failed!", t);
		}
	}

	private Injector getInjector(ServletContext context) {
		Injector injector = null;
		injector = Guice.createInjector(
				Stage.PRODUCTION,
				new CoreModule(context),
				new AmazonWebServicesModule(),
				new QueueModule()
			);
		return injector;
	}
}
