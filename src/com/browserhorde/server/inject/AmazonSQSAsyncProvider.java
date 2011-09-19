package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSQSAsyncProvider implements Provider<AmazonSQSAsync> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSQSAsync get() {
		return new AmazonSQSAsyncClient(awsCredentials, awsConfig, executorService);
	}
}
