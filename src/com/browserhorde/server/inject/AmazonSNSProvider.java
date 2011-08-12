package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSNSProvider implements Provider<AmazonSNS> {
	@Inject private ServletContext context;

	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSNS get() {
		AmazonSNSAsync sns = new AmazonSNSAsyncClient(awsCredentials, awsClientConfig, executorService);

		return sns;
	}

}
