package com.browserhorde.server.inject;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSQSProvider implements Provider<AmazonSQS> {
	@Inject private AmazonSQSAsync awsSQS;

	@Override
	public AmazonSQS get() {
		return awsSQS;
	}
}
