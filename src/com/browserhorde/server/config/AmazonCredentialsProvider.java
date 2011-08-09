package com.browserhorde.server.config;

import javax.servlet.ServletContext;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonCredentialsProvider implements Provider<AWSCredentials> {
	@Inject private ServletContext context;

	@Override
	public AWSCredentials get() {
		String awsAccessKey = context.getInitParameter(ServletInitOptions.AWS_ACCESS_KEY);
		String awsSecretKey = context.getInitParameter(ServletInitOptions.AWS_SECRET_KEY);

		return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	}
}
