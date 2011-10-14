package com.browserhorde.server.aws;

import org.apache.commons.lang3.StringUtils;

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
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.name.Names;

public class AmazonWebServicesModule extends AbstractModule {
	public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
	public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";

	public AmazonWebServicesModule() {

	}

	@Override
	protected void configure() {
		String awsAccessKey = StringUtils.trimToNull(System.getenv(AWS_ACCESS_KEY));
		String awsSecretKey = StringUtils.trimToNull(System.getenv(AWS_SECRET_KEY));

		if(awsAccessKey == null || awsSecretKey == null) {
			throw new NullPointerException();
		}

		bind(String.class).annotatedWith(Names.named(AWS_ACCESS_KEY)).toInstance(awsAccessKey);
		bind(String.class).annotatedWith(Names.named(AWS_SECRET_KEY)).toInstance(awsSecretKey);

		bind(AWSCredentials.class).toProvider(AmazonCredentialsProvider.class).in(Singleton.class);
		bind(ClientConfiguration.class).toProvider(ClientConfigurationProvider.class).in(Singleton.class);

		bind(AmazonElastiCache.class).toProvider(AmazonElastiCacheProvider.class).in(Singleton.class);
		bind(AmazonElastiCacheAsync.class).toProvider(AmazonElastiCacheAsyncProvider.class).in(Singleton.class);

		bind(AmazonS3.class).toProvider(AmazonS3Provider.class).in(Singleton.class);

		bind(AmazonSimpleDB.class).toProvider(AmazonSimpleDBProvider.class).in(Singleton.class);
		bind(AmazonSimpleDBAsync.class).toProvider(AmazonSimpleDBAsyncProvider.class).in(Singleton.class);

		bind(AmazonSimpleEmailService.class).toProvider(AmazonSimpleEmailServiceProvider.class).in(Singleton.class);

		bind(AmazonSNS.class).toProvider(AmazonSNSProvider.class).in(Singleton.class);

		bind(AmazonSQS.class).toProvider(AmazonSQSProvider.class).in(Singleton.class);
		bind(AmazonSQSAsync.class).toProvider(AmazonSQSAsyncProvider.class).in(Singleton.class);
	}
}
