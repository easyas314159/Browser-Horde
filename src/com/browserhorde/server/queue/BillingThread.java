package com.browserhorde.server.queue;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.browserhorde.server.inject.QueueBilling;
import com.browserhorde.server.inject.ThreadGroupMessageHandling;
import com.google.inject.Inject;

public class BillingThread extends MessageHandler {
	@Inject
	protected BillingThread(@ThreadGroupMessageHandling ThreadGroup g, AmazonSQSAsync awsSQS, @QueueBilling String awsQueueUrl) {
		super(g, awsSQS, awsQueueUrl);
	}

	@Override
	protected boolean handleMessage(Message msg) throws Exception {
		return true;
	}

}
