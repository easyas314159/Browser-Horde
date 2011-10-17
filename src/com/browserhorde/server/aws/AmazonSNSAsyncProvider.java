package com.browserhorde.server.aws;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSNSAsyncProvider implements Provider<AmazonSNSAsyncClient> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSNSAsyncClient get() {
		return new AmazonSNSAsyncClient(awsCredentials, awsClientConfig, executorService);
	}

}
