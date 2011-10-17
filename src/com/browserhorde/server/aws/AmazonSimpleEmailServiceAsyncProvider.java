package com.browserhorde.server.aws;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleEmailServiceAsyncProvider implements Provider<AmazonSimpleEmailServiceAsync> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleEmailServiceAsync get() {
		return new AmazonSimpleEmailServiceAsyncClient(awsCredentials, awsClientConfig, executorService);
	}
}
