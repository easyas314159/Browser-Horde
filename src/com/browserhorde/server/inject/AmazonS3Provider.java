package com.browserhorde.server.inject;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonS3Provider implements Provider<AmazonS3> {
	@Inject private AWSCredentials awsCredentials;
	@Inject private ClientConfiguration awsClientConfig;

	@Override
	public AmazonS3 get() {
		return new AmazonS3Client(awsCredentials, awsClientConfig);
	}
}
