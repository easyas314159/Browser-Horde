package com.browserhorde.server.aws;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.elasticache.AmazonElastiCacheAsync;
import com.amazonaws.services.elasticache.AmazonElastiCacheAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonElastiCacheAsyncProvider implements Provider<AmazonElastiCacheAsync> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonElastiCacheAsync get() {
		AmazonElastiCacheAsync sdb = new AmazonElastiCacheAsyncClient(awsCredentials, awsClientConfig, executorService);

		return sdb;
	}
}
