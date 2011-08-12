package com.browserhorde.server.jpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;

public class SimpleDBEntityManagerFactory implements EntityManagerFactory {
	public static final String PARAM_EXECUTOR_SERVICE = "aws.executor_service";

	public static final String PARAM_AWS_S3 = "aws.s3";
	public static final String PARAM_AWS_SDB = "aws.sdb";

	public static final String PARAM_AWS_CREDENTIALS = "aws.credentials";
	public static final String PARAM_AWS_CLIENT_CONFIGURATION = "aws.client_configuration";

	public static final String PARAM_AWS_ACCESS_KEY = "aws.access_key";
	public static final String PARAM_AWS_SECRET_KEY = "aws.secret_key";

	public static final String PARAM_AWS_DOMAIN_MAPPINGS = "aws.domain_mapping";
	public static final String PARAM_AWS_DOMAIN_PREFIX = "aws.domain_prefix";

	private final Logger log = Logger.getLogger(getClass());

	private volatile boolean closed = false;

	private ExecutorService awsExecutorService = null;

	private AWSCredentials awsCredentials = null;
	private ClientConfiguration awsClientConfig = null;

	private AmazonS3 awsS3 = null;
	private AmazonSimpleDB awsSDB = null;
	private AmazonSimpleDBAsync awsSDBAsync = null;

	private String domainPrefix = null;
	private Map<Class<?>, String> domainMapping = null;

	public SimpleDBEntityManagerFactory(PersistenceUnitInfo info, Map<String, Object> map) {
		initAmazonWebServices(map);
	}
	public SimpleDBEntityManagerFactory(String name, Map<String, Object> map) {
		initAmazonWebServices(map);
	}

	private void initAmazonWebServices(Map<String, Object> map) {
		initAWSCredentials(map);
		initAWSClientConfiguration(map);
		initAWSExecutorService(map);

		domainPrefix = (String)map.get(PARAM_AWS_DOMAIN_PREFIX);
		domainMapping = (Map<Class<?>, String>)map.get(PARAM_AWS_DOMAIN_MAPPINGS);
		if(domainMapping == null) {
			domainMapping = new ConcurrentHashMap<Class<?>, String>();
		}
		else {
			domainMapping = new ConcurrentHashMap<Class<?>, String>(domainMapping);
		}

		awsS3 = (AmazonS3)map.get(PARAM_AWS_S3);
		awsSDB = (AmazonSimpleDB)map.get(PARAM_AWS_SDB);

		if(awsSDB instanceof AmazonSimpleDBAsync) {
			awsSDBAsync = (AmazonSimpleDBAsync)awsSDB;
		}
		if(awsS3 == null || awsSDB == null) {
			if(awsS3 == null) {
				awsS3 = new AmazonS3Client(awsCredentials, awsClientConfig);
			}
			if(awsSDB == null) {
				if(awsExecutorService == null) {
					awsSDB = new AmazonSimpleDBClient(awsCredentials, awsClientConfig);
				}
				else {
					awsSDB = awsSDBAsync = new AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, awsExecutorService);
				}
			}
		}
	}

	private void initAWSCredentials(Map<String, Object> map) {
		awsCredentials = (AWSCredentials)map.get(PARAM_AWS_CREDENTIALS);
		if(awsCredentials == null) {
			String accessKey = (String)map.get(PARAM_AWS_ACCESS_KEY);
			String secretKey = (String)map.get(PARAM_AWS_SECRET_KEY);

			if(accessKey == null || secretKey == null) {
				// TODO: Try loading a config file
			}
			else {
				awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
			}
		}
	}
	private void initAWSClientConfiguration(Map<String, Object> map) {
		awsClientConfig = (ClientConfiguration)map.get(PARAM_AWS_CLIENT_CONFIGURATION);

		// TODO: Do a better job of loading client config here
	}
	private void initAWSExecutorService(Map<String, Object> map) {
		awsExecutorService = (ExecutorService)map.get(PARAM_EXECUTOR_SERVICE);
		if(awsExecutorService == null) {
			// TODO: Create a temporary executor
		}
	}

	@Override
	public EntityManager createEntityManager() {
		return createEntityManager(null);
	}
	@Override
	public EntityManager createEntityManager(Map map) {
		return new SimpleDBEntityManager(this, map);
	}

	@Override
	public void close() {
		closed = true;
	}
	@Override
	public boolean isOpen() {
		return !closed;
	}

	public String getDomainName(Class<? extends Object> clazz) {
		String domain = domainMapping.get(clazz);
		if(domain == null) {
			domain = clazz.getName();
			if(domainPrefix != null) {
				domain = domainPrefix + domain;
			}
			log.info(String.format("Class \'%s\' mapped to domain \'%s\'", clazz.getName(), domain));
			domainMapping.put(clazz, domain);
		}
		return domain;
	}

	

	public ExecutorService getExecutorService() {
		return awsExecutorService;
	}

	public AmazonSimpleDB getSimpleDB() {
		return awsSDB;
	}
	public AmazonSimpleDBAsync getSimpleDBAsync() {
		return awsSDBAsync;
	}
}
