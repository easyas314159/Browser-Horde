package com.browserhorde.server.aws;

import java.io.InputStream;
import java.util.concurrent.Callable;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

public class AmazonS3AsyncPutObject implements Callable<PutObjectResult> {
	private AmazonS3 s3Client = null;
	private PutObjectRequest putRequest = null;

	private AccessControlList acl = null;
	private CannedAccessControlList cacl = null;

	public AmazonS3AsyncPutObject(AmazonS3 s3Client, PutObjectRequest putRequest) {
		this.s3Client = s3Client;
		this.putRequest = putRequest;
	}
	public AmazonS3AsyncPutObject(AmazonS3 s3Client, PutObjectRequest putRequest, AccessControlList acl) {
		this(s3Client, putRequest);
		this.acl = acl;
	}
	public AmazonS3AsyncPutObject(AmazonS3 s3Client, PutObjectRequest putRequest, CannedAccessControlList acl) {
		this(s3Client, putRequest);
		this.cacl = acl;
	}

	@Override
	public PutObjectResult call() throws Exception {
		InputStream input = putRequest.getInputStream();
		String bucket = putRequest.getBucketName();
		String key = putRequest.getKey();
		PutObjectResult result = s3Client.putObject(putRequest);
		if(input != null) {
			input.close();
		}
		if(acl != null) {
			s3Client.setObjectAcl(bucket, key, acl);
		}
		else if(cacl != null) {
			s3Client.setObjectAcl(bucket, key, cacl);
		}
		return result;
	}
}
