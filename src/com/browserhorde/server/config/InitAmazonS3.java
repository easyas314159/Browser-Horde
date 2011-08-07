package com.browserhorde.server.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Region;
import com.browserhorde.server.Configurator;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.aws.BucketManager;
import com.browserhorde.server.util.ParamUtils;

public class InitAmazonS3 extends ConfigCommand {
	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		ServletContext context = ctx.getServletContext();

		AWSCredentials awsCredentials = (AWSCredentials)context.getAttribute(Configurator.AWS_CREDENTIALS);
		ClientConfiguration awsClientConfig = (ClientConfiguration)context.getAttribute(Configurator.AWS_CLIENT_CONFIG);

		String s3BucketPrefix = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET_PREFIX));
		String s3Buckets = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_S3_BUCKETS));

		BucketManager.setBucketPrefix(s3BucketPrefix);
		AmazonS3 s3 = new AmazonS3Client(awsCredentials, awsClientConfig);

		Set<String> newBuckets = new HashSet<String>();
		String rawBuckets[] = s3Buckets.split(";");

		for(String bucket : rawBuckets) {
			bucket = StringUtils.trimToNull(bucket);
			if(bucket != null) {
				bucket = BucketManager.getBucket(bucket);
				newBuckets.add(bucket);
			}
		}

		List<Bucket> buckets = s3.listBuckets();
		for(Bucket bucket : buckets) {
			newBuckets.remove(bucket.getName());
		}
		for(String bucket : newBuckets) {
			log.info(String.format("Creating bucket: \'%s\'", bucket));
			s3.createBucket(bucket, Region.US_Standard);
		}

		context.setAttribute(Configurator.AWS_S3, s3);

		return false;
	}
}
