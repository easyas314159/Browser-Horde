package com.browserhorde.server;

import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.log4j.Logger;

import com.browserhorde.server.config.ConfigContext;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	// Thread Pool
	public static final String EXECUTOR_SERVICE = "executor_service";
	
	// Memcached
	public static final String MEMCAHED = "memcached";

	// Amazon Web Services
	public static final String AWS_CREDENTIALS = "aws_credentials";
	public static final String AWS_CLIENT_CONFIG = "aws_client_configuration";
	
	public static final String AWS_S3 = "aws_s3";
	public static final String AWS_SDB = "aws_sdb";
	public static final String AWS_SES = "aws_ses";

	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();

		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

		try {
			ConfigParser parser = new ConfigParser();
			parser.parse(context.getResource("/WEB-INF/catalog.xml"));
			Catalog catalog = CatalogFactoryBase.getInstance().getCatalog();

			ConfigContext cmdctx = new ConfigContext(context);

			Command cmd = catalog.getCommand("Startup");
			cmd.execute(cmdctx);
		}
		catch(Throwable t) {
			log.fatal("Startup Failed!", t);
		}
	}

	public void contextDestroyed(ServletContextEvent eventt) {
	}
}
