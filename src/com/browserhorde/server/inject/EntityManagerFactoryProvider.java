package com.browserhorde.server.inject;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Result;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.jpa.SimpleJPAEntityManagerFactory;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import com.spaceprogram.simplejpa.cache.KittyCacheFactory;

@SuppressWarnings("deprecation")
public class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject @Named(ServletInitOptions.AWS_SDB_DOMAIN_PREFIX) private String awsDomainPrefix;

	@Inject private AmazonS3 awsS3;
	@Inject private AmazonSimpleDB awsSDB;
	

	@Override
	public EntityManagerFactory get() {
		Map<String, Object> params = new HashMap<String, Object>();

		params.put(SimpleJPAEntityManagerFactory.PARAM_AWS_S3, awsS3);
		params.put(SimpleJPAEntityManagerFactory.PARAM_AWS_SDB, awsSDB);

		params.put(SimpleJPAEntityManagerFactory.PARAM_CACHE_FACTORY, KittyCacheFactory.class);

		params.put(SimpleJPAEntityManagerFactory.PARAM_AWS_DOMAIN_PREFIX, awsDomainPrefix);
		
		Map<Class<?>, String> domainMappings = new HashMap<Class<?>, String>();
		params.put(SimpleJPAEntityManagerFactory.PARAM_AWS_DOMAIN_MAPPINGS, domainMappings);

		domainMappings.put(User.class, awsDomainPrefix + "users");
		domainMappings.put(Job.class, awsDomainPrefix + "jobs");
		domainMappings.put(Script.class, awsDomainPrefix + "scripts");
		domainMappings.put(Task.class, awsDomainPrefix + "tasks");
		domainMappings.put(Result.class, awsDomainPrefix + "results");

		// TODO: These are only here to work around SimpleJPAs limitations
		params.put("cacheFactory", null);
		params.put("accessKey", "access");
		params.put("secretKey", "secret");

		EntityManagerFactory factory = null;
		try {
			factory = new SimpleJPAEntityManagerFactory(awsDomainPrefix, params);
		}
		catch(Throwable t) {
			log.fatal("Failed to create EntityManagerFactory", t);
		}
		return factory;
	}
}
