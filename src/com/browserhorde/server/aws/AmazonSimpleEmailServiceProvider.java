package com.browserhorde.server.aws;

import java.util.concurrent.ExecutorService;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleEmailServiceProvider implements Provider<AmazonSimpleEmailService> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleEmailService get() {
		return new AmazonSimpleEmailServiceAsyncClient(awsCredentials, awsClientConfig, executorService);
	}
}
