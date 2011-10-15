package com.browserhorde.server.queue;

import java.io.BufferedOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.browserhorde.server.gson.GsonUtils;
import com.browserhorde.server.inject.QueueGZIP;
import com.browserhorde.server.inject.QueueMinify;
import com.browserhorde.server.inject.ThreadGroupMessageHandling;
import com.browserhorde.server.util.ConcurrentPipeStream;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class MinifyThread extends MessageHandler {
	private final AmazonSQSAsync awsSQS;
	private final AmazonS3 awsS3;
	private final String awsQueueGzip;
	private final Gson gson;

	@Inject
	public MinifyThread(@ThreadGroupMessageHandling ThreadGroup g, AmazonSQSAsync awsSQS, AmazonS3 awsS3, @QueueMinify String awsQueueMinify, @QueueGZIP String awsQueueGzip) {
		super(g, awsSQS, awsQueueMinify);

		this.awsS3 = awsS3;
		this.awsSQS = awsSQS;
		this.awsQueueGzip = awsQueueGzip;

		this.gson = GsonUtils.newGson();
	}

	@Override
	protected boolean handleMessage(Message msg) throws Exception {
		ProcessObject o = gson.fromJson(msg.getBody(), ProcessObject.class);
		try {
			minifyObject(o.getBucket(), o.getKey());
			return true;
		}
		catch(Throwable t) {
			log.error(String.format("%s:%s", o.getBucket(), o.getKey()), t);
		}
		return false;
	}

	private void minifyObject(String bucket, String key) throws Exception {
		S3Object object = awsS3.getObject(bucket, key);
		ObjectMetadata metadata = object.getObjectMetadata();
		AccessControlList acl = awsS3.getObjectAcl(bucket, key);

		log.info(String.format("%s:%s", bucket, key));

		key = key + ".min";

		ConcurrentPipeStream pipe = new ConcurrentPipeStream();
		BufferedOutputStream outBuffered = new BufferedOutputStream(pipe.getOutputStream());
		CountingOutputStream outCount = new CountingOutputStream(outBuffered);
		OutputStreamWriter writer = new OutputStreamWriter(outCount);

		InputStreamReader reader = new InputStreamReader(object.getObjectContent());

		JavaScriptCompressor compressor = new JavaScriptCompressor(reader, new Feedback());
		compressor.compress(writer, 16000, true, true, false, false);

		IOUtils.closeQuietly(writer);
		IOUtils.closeQuietly(reader);

		metadata.setContentLength(outCount.getByteCount());
		awsS3.putObject(bucket, key, pipe.getInputStream(), metadata);
		awsS3.setObjectAcl(bucket, key, acl);

		ProcessObject gzip = new ProcessObject(bucket, key);
		awsSQS.sendMessageAsync(new SendMessageRequest(awsQueueGzip, gson.toJson(gzip)));
	}

	private class Feedback implements ErrorReporter {
		@Override
		public void warning(String arg0, String arg1, int arg2, String arg3, int arg4) {
		}
		@Override
		public void error(String arg0, String arg1, int arg2, String arg3, int arg4) {
		}

		@Override
		public EvaluatorException runtimeError(String arg0, String arg1, int arg2, String arg3, int arg4) {
			return null;
		}

	}
}
