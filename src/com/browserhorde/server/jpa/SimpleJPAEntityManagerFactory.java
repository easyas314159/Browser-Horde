package com.browserhorde.server.jpa;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.spaceprogram.simplejpa.EntityManagerFactoryImpl;

/**
 * 
 * @author kloney
 * 
 * @deprecated This is simply a work around for the shoddy implementation of SimpleJPA
 */
@Deprecated
public class SimpleJPAEntityManagerFactory extends EntityManagerFactoryImpl {
	public static final String PARAM_EXECUTOR_SERVICE = "aws.executor_service";

	public static final String PARAM_AWS_S3 = "aws.s3";
	public static final String PARAM_AWS_SDB = "aws.sdb";

	public static final String PARAM_AWS_CREDENTIALS = "aws.credentials";
	public static final String PARAM_AWS_CLIENT_CONFIGURATION = "aws.client_configuration";

	public static final String PARAM_AWS_ACCESS_KEY = "aws.access_key";
	public static final String PARAM_AWS_SECRET_KEY = "aws.secret_key";

	public static final String PARAM_AWS_DOMAIN_MAPPINGS = "aws.domain_mapping";
	public static final String PARAM_AWS_DOMAIN_PREFIX = "aws.domain_prefix";

	private ExecutorService awsExecutorService = null;

	private AWSCredentials awsCredentials = null;
	private ClientConfiguration awsClientConfig = null;

	private AmazonS3 awsS3 = null;
	private AmazonSimpleDB awsSDB = null;

	@SuppressWarnings("unchecked")
	public SimpleJPAEntityManagerFactory(String persistenceUnitName, Map props) {
		super(persistenceUnitName, props);

		initAmazonWebServices(props);
	}

	private void initAmazonWebServices(Map<String, Object> map) {
		initAWSCredentials(map);
		initAWSClientConfiguration(map);
		initAWSExecutorService(map);

		awsS3 = (AmazonS3)map.get(PARAM_AWS_S3);
		awsSDB = (AmazonSimpleDB)map.get(PARAM_AWS_SDB);

		if(awsS3 == null || awsSDB == null) {
			if(awsS3 == null) {
				awsS3 = new AmazonS3Client(awsCredentials, awsClientConfig);
			}
			if(awsSDB == null) {
				if(awsExecutorService == null) {
					awsSDB = new AmazonSimpleDBClient(awsCredentials, awsClientConfig);
				}
				else {
					awsSDB = new AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, awsExecutorService);
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
	public AmazonSimpleDB getSimpleDb() {
		return awsSDB;
	}

	@Override
	public AmazonS3 getS3Service() {
		return awsS3;
	}

	@Override
	public ExecutorService getExecutor() {
		return awsExecutorService;
	}
}
