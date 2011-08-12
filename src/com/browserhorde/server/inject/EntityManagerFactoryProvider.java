package com.browserhorde.server.inject;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Result;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.jpa.SimpleDBEntityManagerFactory;
import com.browserhorde.server.jpa.SimpleJPAEntityManagerFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private ServletContext context;

	@Inject private AmazonS3 awsS3;
	@Inject private AmazonSimpleDB awsSDB;

	@Override
	public EntityManagerFactory get() {
		String persistenceUnitName = context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAIN_PREFIX);

		Map<String, Object> params = new HashMap<String, Object>();

		params.put(SimpleDBEntityManagerFactory.PARAM_AWS_S3, awsS3);
		params.put(SimpleDBEntityManagerFactory.PARAM_AWS_SDB, awsSDB);

		String domainPrefix = context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAIN_PREFIX);
		params.put(SimpleDBEntityManagerFactory.PARAM_AWS_DOMAIN_PREFIX, domainPrefix);
		
		Map<Class<?>, String> domainMappings = new HashMap<Class<?>, String>();
		params.put(SimpleDBEntityManagerFactory.PARAM_AWS_DOMAIN_MAPPINGS, domainMappings);

		domainMappings.put(User.class, domainPrefix + "users");
		domainMappings.put(Job.class, domainPrefix + "jobs");
		domainMappings.put(Script.class, domainPrefix + "scripts");
		domainMappings.put(Task.class, domainPrefix + "tasks");
		domainMappings.put(Result.class, domainPrefix + "results");

		// TODO: These are only here to work around SimpleJPAs limitations
		params.put("cacheFactory", null);
		params.put("accessKey", "access");
		params.put("secretKey", "secret");

		EntityManagerFactory factory = null;
		try {
			factory = new SimpleJPAEntityManagerFactory(persistenceUnitName, params);
		}
		catch(Throwable t) {
			log.fatal("Failed to create EntityManagerFactory", t);
		}
		return factory;
	}
}
