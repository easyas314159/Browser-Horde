package com.browserhorde.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.sns.AmazonSNS;
import com.browserhorde.server.config.AmazonCredentialsProvider;
import com.browserhorde.server.config.AmazonS3Provider;
import com.browserhorde.server.config.AmazonSNSProvider;
import com.browserhorde.server.config.AmazonSimpleDBProvider;
import com.browserhorde.server.config.AmazonSimpleEmailServiceProvider;
import com.browserhorde.server.config.ClientConfigurationProvider;
import com.browserhorde.server.config.ExecutorServiceProvider;
import com.browserhorde.server.config.MemcachedClientProvider;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class DependencyInjection extends GuiceServletContextListener {
	private Logger log = Logger.getLogger(getClass());

	@Override
	protected Injector getInjector() {
		Injector injector = null;
		try {
			injector = Guice.createInjector(
					new JerseyServletModule() {
						@Override
						protected void configureServlets() {
							bind(ExecutorService.class)
								.toProvider(ExecutorServiceProvider.class)
								.in(Singleton.class);

							bind(MemcachedClient.class)
								.toProvider(MemcachedClientProvider.class)
								.in(Singleton.class);

							bind(AWSCredentials.class)
								.toProvider(AmazonCredentialsProvider.class)
								.in(Singleton.class);
							bind(ClientConfiguration.class)
								.toProvider(ClientConfigurationProvider.class)
								.in(Singleton.class);
							bind(AmazonS3.class)
								.toProvider(AmazonS3Provider.class)
								.in(Singleton.class);
							bind(AmazonSimpleDB.class)
								.toProvider(AmazonSimpleDBProvider.class)
								.in(Singleton.class);
							bind(AmazonSimpleEmailService.class)
								.toProvider(AmazonSimpleEmailServiceProvider.class)
								.in(Singleton.class);
							bind(AmazonSNS.class)
								.toProvider(AmazonSNSProvider.class)
								.in(Singleton.class);

							Map<String, String> params = new HashMap<String, String>();
			                params.put(
			                		PackagesResourceConfig.PROPERTY_PACKAGES,
			                		"com.browserhorde.server.api;com.browserhorde.server.aws"
			                	);
							serve("*")
								.with(GuiceContainer.class, params);
						}
						
					}
				);
		}
		catch(Throwable t) {
			log.fatal("", t);
		}
		return injector;
	}
}
