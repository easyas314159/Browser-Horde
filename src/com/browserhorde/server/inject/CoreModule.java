package com.browserhorde.server.inject;

import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;

import com.browserhorde.server.ErrorFilter;
import com.browserhorde.server.ExecutorServiceListener;
import com.browserhorde.server.MemcachedClientListener;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.XMachineIdFilter;
import com.browserhorde.server.XRuntime;
import com.browserhorde.server.cors.PreflightHijackFilter;
import com.browserhorde.server.security.AuthenticationFilter;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Scope;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistFilter;
import com.google.inject.persist.jpa.JpaPersistModule;
import com.google.inject.servlet.RequestScoped;
import com.google.inject.util.Providers;
import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

public class CoreModule extends JerseyServletModule {
	private final Logger log = Logger.getLogger(getClass());

	private final ServletContext context;

	public CoreModule(ServletContext context) {
		this.context = context;
	}

	@Override
	protected void configureServlets() {
		try {
			bindServletInitOptions();

			bind(Random.class).toProvider(RandomProvider.class).in(
					RequestScoped.class);

			bind(Principal.class)
				.toProvider(PrincipalProvider.class)
				.in(RequestScoped.class);
			bind(ResourceBundle.class)
				.toProvider(ResourceBundleProvider.class)
				.in(RequestScoped.class);

			Map<String, String> params = new HashMap<String, String>();
			params.put(PackagesResourceConfig.PROPERTY_PACKAGES,
					"com.browserhorde.server.api;");
			params.put(ResourceConfig.PROPERTY_RESOURCE_FILTER_FACTORIES,
					RolesAllowedResourceFilterFactory.class.getName());

			configureJpa();
			
			bindFilters();

			serve("/*").with(GuiceContainer.class, params);
		} catch(Throwable t) {
			log.fatal("DI Configuration Failed!", t);
		}
	}

	private void configureJpa() {
		// JPA Configuration
		String jpaUnit = ParamUtils.asString(
				context.getInitParameter(ServletInitOptions.JPA_UNIT), "default"
			);
		Properties properties = new Properties();
		JpaPersistModule jpaModule = new JpaPersistModule(jpaUnit);

		jpaModule.properties(properties);

		install(jpaModule);
	}
	
	private void bindFilters() {
		filter("/*").through(XRuntime.class);
		filter("/*").through(ErrorFilter.class);

		// FIXME: Rate limiter is currently broken
		// filter("/*").through(XRateLimitFilter.class);

		filter("/*").through(new PreflightHijackFilter());

		filter("/*").through(PersistFilter.class);
		filter("/*").through(AuthenticationFilter.class);
		filter("/workorders/*").through(XMachineIdFilter.class);
	}

	private <T> void bindNamed(Class<T> clazz, String name, T value) {
		bind(clazz).annotatedWith(Names.named(name)).toProvider(
				Providers.of(value));
	}

	private void bindServletInitOptions() {
		Properties p = new Properties();
		Enumeration<String> params = context.getInitParameterNames();
		while(params.hasMoreElements()) {
			String key = params.nextElement();
			String value = context.getInitParameter(key);

			p.setProperty(key, value);
		}

		p = new Properties(p);
		p.putAll(System.getenv());

		String awsS3Bucket = p.getProperty(ServletInitOptions.AWS_S3_BUCKET);
		String awsS3BucketEndpoint = String.format("%s.s3.amazonaws.com",
				awsS3Bucket);

		ExecutorService executorService = (ExecutorService) context
				.getAttribute(ExecutorServiceListener.EXECUTOR_NAME);
		bind(ExecutorService.class).toInstance(executorService);

		MemcachedClient memcachedClient = (MemcachedClient) context
				.getAttribute(MemcachedClientListener.MEMCACHED_NAME);
		if(memcachedClient == null) {
			bind(MemcachedClient.class).toProvider(
					Providers.of((MemcachedClient) null));
		} else {
			bind(MemcachedClient.class).toInstance(memcachedClient);
		}

		bindNamed(String.class, ServletInitOptions.AWS_S3_BUCKET, awsS3Bucket);
		bindNamed(
				String.class,
				ServletInitOptions.AWS_S3_BUCKET_ENDPOINT,
				ParamUtils.asString(
						p.getProperty(ServletInitOptions.AWS_S3_BUCKET_ENDPOINT),
						awsS3BucketEndpoint));

		bindNamed(String.class, ServletInitOptions.AWS_SDB_DOMAIN_PREFIX,
				p.getProperty(ServletInitOptions.AWS_SDB_DOMAIN_PREFIX));

		bindNamed(String.class, ServletInitOptions.AWS_SQS_PREFIX,
				p.getProperty(ServletInitOptions.AWS_SQS_PREFIX));

		bindNamed(String.class, ServletInitOptions.AWS_SES_SENDER,
				p.getProperty(ServletInitOptions.AWS_SES_SENDER));
	}
}
