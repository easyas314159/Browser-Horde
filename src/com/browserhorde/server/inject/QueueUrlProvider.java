package com.browserhorde.server.inject;

import javax.annotation.Nullable;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.CreateQueueResult;
import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class QueueUrlProvider implements Provider<String> {
	private final String queueName;

	@Inject private AmazonSQSAsync awsSQS;
	@Inject @Nullable @Named(ServletInitOptions.AWS_SQS_PREFIX) private String awsSQSPrefix;

	public QueueUrlProvider(String queueName) {
		this.queueName = queueName;
	}

	@Override
	public String get() {
		String name = queueName;
		if(awsSQSPrefix != null) {
			name = String.format("%s-%s", awsSQSPrefix, name);
		}

		CreateQueueResult result = awsSQS.createQueue(new CreateQueueRequest(name));
		return result.getQueueUrl();
	}

}
