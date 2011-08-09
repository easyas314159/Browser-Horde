package com.browserhorde.server.config;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleDBProvider implements Provider<AmazonSimpleDB> {
	@Inject private ServletContext context;

	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;
	@Inject private ExecutorService executorService;

	@Override
	public AmazonSimpleDB get() {
		AmazonSimpleDBAsync sdb = new  AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, executorService);

		return sdb;
	}
}
