package com.browserhorde.server.queue;

import java.io.BufferedOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.browserhorde.server.gson.GsonUtils;
import com.browserhorde.server.inject.QueueGZIP;
import com.browserhorde.server.util.ConcurrentPipeStream;
import com.google.gson.Gson;
import com.google.inject.Inject;

public class GzipProcessor extends MessageHandler {
	private final Logger log = Logger.getLogger(getClass());

	private final AmazonS3 awsS3;
	private final Gson gson;
	
	@Inject
	public GzipProcessor(AmazonSQSAsync awsSQS, AmazonS3 awsS3, @QueueGZIP String awsQueueGzip) {
		super(awsSQS, awsQueueGzip);
		this.awsS3 = awsS3;
		this.gson = GsonUtils.newGson();
	}

	@Override
	protected boolean handleMessage(Message msg) throws Exception {
		String body = msg.getBody();

		ProcessObject o = gson.fromJson(body, ProcessObject.class);

		try {
			gzipObject(o.getBucket(), o.getKey());
			return true;
		}
		catch(Throwable t) {
			log.error(String.format("%s:%s", o.getBucket(), o.getKey()), t);
		}
		return false;
	}

	private void gzipObject(String bucket, String key) throws Exception {
		S3Object object = awsS3.getObject(bucket, key);
		ObjectMetadata metadata = object.getObjectMetadata();
		AccessControlList acl = awsS3.getObjectAcl(bucket, key);

		log.info(String.format("%s:%s", bucket, key));

		key = key + ".gz";
		metadata.setContentEncoding("gzip");

		ConcurrentPipeStream pipe = new ConcurrentPipeStream();

		BufferedOutputStream outBuffered = new BufferedOutputStream(pipe.getOutputStream());
		CountingOutputStream outCount = new CountingOutputStream(outBuffered);
		GZIPOutputStream outGzip = new GZIPOutputStream(outCount);
		IOUtils.copy(object.getObjectContent(), outGzip);

		IOUtils.closeQuietly(object.getObjectContent());
		IOUtils.closeQuietly(outGzip);

		metadata.setContentLength(outCount.getByteCount());
		awsS3.putObject(bucket, key, pipe.getInputStream(), metadata);
		awsS3.setObjectAcl(bucket, key, acl);
	}
}
