package com.browserhorde.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContextEvent;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheAsync;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.browserhorde.server.inject.AmazonCredentialsProvider;
import com.browserhorde.server.inject.AmazonElastiCacheAsyncProvider;
import com.browserhorde.server.inject.AmazonElastiCacheProvider;
import com.browserhorde.server.inject.AmazonS3Provider;
import com.browserhorde.server.inject.AmazonSNSProvider;
import com.browserhorde.server.inject.AmazonSQSAsyncProvider;
import com.browserhorde.server.inject.AmazonSQSProvider;
import com.browserhorde.server.inject.AmazonSimpleDBAsyncProvider;
import com.browserhorde.server.inject.AmazonSimpleDBProvider;
import com.browserhorde.server.inject.AmazonSimpleEmailServiceProvider;
import com.browserhorde.server.inject.ClientConfigurationProvider;
import com.browserhorde.server.inject.EntityManagerFactoryProvider;
import com.browserhorde.server.inject.EntityManagerProvider;
import com.browserhorde.server.inject.ExecutorServiceProvider;
import com.browserhorde.server.inject.FileItemFactoryProvider;
import com.browserhorde.server.inject.MemcachedClientProvider;
import com.browserhorde.server.inject.RandomProvider;
import com.browserhorde.server.security.AuthenticationFilter;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.RequestScoped;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class DependencyInjection extends GuiceServletContextListener {
	private Logger log = Logger.getLogger(getClass());

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		try {
			super.contextInitialized(servletContextEvent);
		}
		catch(Throwable t) {
			log.fatal("DI Initialization Failed!", t);
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		try {			
			super.contextDestroyed(servletContextEvent);
		}
		catch(Throwable t) {
			log.fatal("DI Destruction Failed!", t);
		}
	}

	@Override
	protected Injector getInjector() {
		Injector injector = null;
		try {
			injector = Guice.createInjector(
					new JerseyServletModule() {
						@Override
						protected void configureServlets() {
							try {
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
								bind(AmazonElastiCache.class)
									.toProvider(AmazonElastiCacheProvider.class)
									.in(Singleton.class);
								bind(AmazonElastiCacheAsync.class)
									.toProvider(AmazonElastiCacheAsyncProvider.class)
									.in(Singleton.class);
								bind(AmazonS3.class)
									.toProvider(AmazonS3Provider.class)
									.in(Singleton.class);
								bind(AmazonSimpleDB.class)
									.toProvider(AmazonSimpleDBProvider.class)
									.in(Singleton.class);
								bind(AmazonSimpleDBAsync.class)
									.toProvider(AmazonSimpleDBAsyncProvider.class)
									.in(Singleton.class);
								bind(AmazonSimpleEmailService.class)
									.toProvider(AmazonSimpleEmailServiceProvider.class)
									.in(Singleton.class);
								bind(AmazonSNS.class)
									.toProvider(AmazonSNSProvider.class)
									.in(Singleton.class);
								bind(AmazonSQS.class)
									.toProvider(AmazonSQSProvider.class)
									.in(Singleton.class);
								bind(AmazonSQSAsync.class)
									.toProvider(AmazonSQSAsyncProvider.class)
									.in(Singleton.class);
	
								bind(EntityManagerFactory.class)
									.toProvider(EntityManagerFactoryProvider.class)
									.in(Singleton.class);
								bind(EntityManager.class)
									.toProvider(EntityManagerProvider.class)
									.in(RequestScoped.class);
								
								bind(FileItemFactory.class)
									.toProvider(FileItemFactoryProvider.class)
									.in(Singleton.class);
								bind(Random.class)
									.toProvider(RandomProvider.class)
									.in(RequestScoped.class);
	
								Map<String, String> params = new HashMap<String, String>();
				                params.put(
				                		PackagesResourceConfig.PROPERTY_PACKAGES,
				                		"com.browserhorde.server.api;"
				                	);
				                params.put(
				                		"com.sun.jersey.spi.container.ResourceFilters",
				                		RolesAllowedResourceFilterFactory.class.getName()
				                	);
	
				                // FIXME: Rate limiter is currently broken
				                //filter("/*").through(XRateLimitFilter.class);
				                filter("/*").through(AuthenticationFilter.class);
				                filter("/workorders/*").through(XMachineIdFilter.class);
	
								serve("/*")
									.with(GuiceContainer.class, params)
									;
							}
							catch(Throwable t) {
								log.fatal("DI Configuration Failed!", t);
							}
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
