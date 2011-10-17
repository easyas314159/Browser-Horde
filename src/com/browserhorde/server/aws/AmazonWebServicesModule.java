package com.browserhorde.server.aws;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheAsync;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class AmazonWebServicesModule extends AbstractModule {
	private final AWSCredentials awsCredentials;
	private final ClientConfiguration awsClientConfig;

	public AmazonWebServicesModule(AWSCredentials awsCredentials) {
		this(awsCredentials, new ClientConfiguration());
	}
	public AmazonWebServicesModule(AWSCredentials awsCredentials, ClientConfiguration awsClientConfig) {
		this.awsCredentials = awsCredentials;
		this.awsClientConfig = awsClientConfig;
	}

	@Override
	protected void configure() {
		bind(AWSCredentials.class).toInstance(awsCredentials);
		bind(ClientConfiguration.class).toInstance(awsClientConfig);

		bind(AmazonElastiCacheAsync.class).toProvider(AmazonElastiCacheAsyncProvider.class).in(Singleton.class);
		bind(AmazonElastiCache.class).to(AmazonElastiCacheAsync.class).in(Singleton.class);

		bind(AmazonS3.class).toProvider(AmazonS3Provider.class).in(Singleton.class);

		bind(AmazonSimpleDBAsync.class).toProvider(AmazonSimpleDBAsyncProvider.class).in(Singleton.class);
		bind(AmazonSimpleDB.class).to(AmazonSimpleDBAsync.class).in(Singleton.class);

		bind(AmazonSimpleEmailServiceAsync.class).toProvider(AmazonSimpleEmailServiceAsyncProvider.class).in(Singleton.class);
		bind(AmazonSimpleEmailService.class).to(AmazonSimpleEmailServiceAsync.class).in(Singleton.class);

		bind(AmazonSNSAsync.class).toProvider(AmazonSNSAsyncProvider.class).in(Singleton.class);
		bind(AmazonSNS.class).to(AmazonSNSAsync.class).in(Singleton.class);

		bind(AmazonSQSAsync.class).toProvider(AmazonSQSAsyncProvider.class).in(Singleton.class);
		bind(AmazonSQS.class).to(AmazonSQSAsync.class).in(Singleton.class);
	}
}
