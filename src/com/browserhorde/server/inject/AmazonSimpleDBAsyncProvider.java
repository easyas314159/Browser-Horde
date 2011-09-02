package com.browserhorde.server.inject;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleDBAsyncProvider implements Provider<AmazonSimpleDBAsync> {
	@Inject private ServletContext context;

	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleDBAsync get() {
		AmazonSimpleDBAsync sdb = new  AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, executorService);

		return sdb;
	}
}
