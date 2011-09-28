package com.browserhorde.server.inject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class AmazonCredentialsProvider implements Provider<AWSCredentials> {
	@Inject @Named(ServletInitOptions.AWS_ACCESS_KEY) private String awsAccessKey;
	@Inject @Named(ServletInitOptions.AWS_SECRET_KEY) private String awsSecretKey;

	@Override
	public AWSCredentials get() {
		return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	}
}
