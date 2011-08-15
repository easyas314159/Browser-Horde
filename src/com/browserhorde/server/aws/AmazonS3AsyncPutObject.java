package com.browserhorde.server.aws;

import java.io.InputStream;
import java.util.concurrent.Callable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

public class AmazonS3AsyncPutObject implements Callable<PutObjectResult> {
	private AmazonS3 s3Client = null;
	private PutObjectRequest putRequest = null;

	public AmazonS3AsyncPutObject(AmazonS3 s3Client, PutObjectRequest putRequest) {
		this.s3Client = s3Client;
		this.putRequest = putRequest;
	}

	@Override
	public PutObjectResult call() throws Exception {
		InputStream input = putRequest.getInputStream();
		PutObjectResult result = s3Client.putObject(putRequest);
		if(input != null) {
			input.close();
		}
		return result;
	}
}
