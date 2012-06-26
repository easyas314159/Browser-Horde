package com.browserhorde.server.queue;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.browserhorde.server.util.ConcurrentPipeStream;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

public class MinifyAction extends DeferredAction {
	private final AmazonS3 awsS3;

	private final String bucket, srcKey, dstKey;

	public MinifyAction(AmazonS3 awsS3, String bucket, String srcKey, String dstKey) {
		this.awsS3 = awsS3;
		this.bucket = bucket;

		this.srcKey = srcKey;
		this.dstKey = dstKey;
	}

	@Override
	public int getMaxAttempts() {
		return 10;
	}
	
	@Override
	public void execute() throws Exception {
		S3Object object = awsS3.getObject(bucket, srcKey);
		ObjectMetadata metadata = object.getObjectMetadata();
		AccessControlList acl = awsS3.getObjectAcl(bucket, srcKey);

		InputStreamReader reader = new InputStreamReader(object.getObjectContent());

		ConcurrentPipeStream pipe = new ConcurrentPipeStream();
		CountingOutputStream outCount = new CountingOutputStream(pipe.getOutputStream());
		OutputStreamWriter writer = new OutputStreamWriter(outCount);

		JavaScriptCompressor compressor = new JavaScriptCompressor(reader, null);
		compressor.compress(writer, 16384, true, true, false, false);

		IOUtils.closeQuietly(object.getObjectContent());
		IOUtils.closeQuietly(writer);

		metadata.setContentLength(outCount.getByteCount());

		awsS3.putObject(bucket, dstKey, pipe.getInputStream(), metadata);
		awsS3.setObjectAcl(bucket, dstKey, acl);
	}
}
