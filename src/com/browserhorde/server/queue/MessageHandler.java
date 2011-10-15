package com.browserhorde.server.queue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

public abstract class MessageHandler extends Thread {
	protected final Logger log = Logger.getLogger(getClass());

	private final AmazonSQSAsync awsSQS;
	private final ReceiveMessageRequest receiveRequest;

	private long minRest = 1000L;
	private long maxRest = 128000L;

	private long rest = minRest;

	private AtomicBoolean active = new AtomicBoolean(true);

	protected MessageHandler(ThreadGroup g, AmazonSQSAsync awsSQS, String awsQueueUrl) {
		super(g, UUID.randomUUID().toString());

		this.awsSQS = awsSQS;
		this.receiveRequest = new ReceiveMessageRequest(awsQueueUrl).withMaxNumberOfMessages(10);
	}

	@Override
	public void run() {
		while(active.get()) {
			ReceiveMessageResult result = awsSQS.receiveMessage(receiveRequest);
			List<Message> messages = result.getMessages();

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

			// TODO: Do a better job of anticipating the rate of requests
			if(messages.isEmpty()) {
				rest = Math.min(maxRest, rest << 1);
			}
			else if(messages.size() < receiveRequest.getMaxNumberOfMessages()) {
				rest = minRest;
			}
			else {
				rest = 0L;
			}

			try {
				sleep(rest);
			}
			catch(InterruptedException ex) {
				log.info("Thread Interrupted", ex);
			}
		}
	}

	public void shutdown() {
		active.set(false);
	}

	protected abstract boolean handleMessage(Message msg) throws Exception;
}
