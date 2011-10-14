package com.browserhorde.server.queue;

import org.apache.log4j.Logger;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public abstract class MessageHandler implements Runnable {
	protected final Logger log = Logger.getLogger(getClass());

	private final AmazonSQSAsync awsSQS;
	private final ReceiveMessageRequest receiveRequest;

	protected MessageHandler(AmazonSQSAsync awsSQS, String awsQueueUrl) {
		this.awsSQS = awsSQS;
		this.receiveRequest = new ReceiveMessageRequest(awsQueueUrl);
	}

	@Override
	public void run() {
		ReceiveMessageResult result = awsSQS.receiveMessage(receiveRequest);
		for(Message msg : result.getMessages()) {
			try {
				if(handleMessage(msg)) {
					awsSQS.deleteMessage(new DeleteMessageRequest(receiveRequest.getQueueUrl(), msg.getReceiptHandle()));
				}
			}
			catch(Exception ex) {
				log.error(String.format("Message Id: %s", msg.getMessageId()), ex);
			}
		}
	}

	protected abstract boolean handleMessage(Message msg) throws Exception;
}
