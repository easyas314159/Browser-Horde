package com.browserhorde.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.browserhorde.server.aws.AmazonWebServicesModule;
import com.browserhorde.server.gson.GsonModule;
import com.browserhorde.server.inject.CoreModule;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.persist.jpa.JpaPersistModule

public class InjectionListener implements ServletContextListener {
	public static final String INJECTOR_NAME = Injector.class.getName();

	private Logger log = Logger.getLogger(getClass());

	private AWSCredentials awsCredentials;

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		try {
			ServletContext context = evt.getServletContext();

			String awsAccessKey = ParamUtils.coalesce(
					context.getInitParameter(ServletInitOptions.AWS_ACCESS_KEY),
					System.getenv().get(ServletInitOptions.AWS_ACCESS_KEY)
				);
			String awsSecretKey = ParamUtils.coalesce(
					context.getInitParameter(ServletInitOptions.AWS_SECRET_KEY),
					System.getenv().get(ServletInitOptions.AWS_SECRET_KEY)
				);
			awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

			State stage = ParamUtils.asEnum(
					Stage.class,
					context.getInitParameter(ServletInitOptions.GUICE_STAGE),
					Stage.PRODUCTION
				);

			Injector injector = getInjector(context, stage);
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

	private Injector getInjector(ServletContext context, Stage stage) {
		Injector injector = null;
		injector = Guice.createInjector(
				Stage.PRODUCTION,
				new CoreModule(context),
				new GsonModule(),
				new AmazonWebServicesModule(awsCredentials),
				new JpaPersistModule(context.getInitParameter(ServletInitOptions.JPA_UNIT));
			);
		return injector;
	}
}
