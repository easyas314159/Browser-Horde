package com.browserhorde.server.aws;

public final class BucketManager {
	private static String bucketPrefix = null;

	private BucketManager() {}

	public static void setBucketPrefix(String prefix) {
		BucketManager.bucketPrefix = prefix;
	}
	public static String getBucketPrefix() {
		return bucketPrefix;
	}

	public static String getBucket(String name) {
		return bucketPrefix == null ? name : bucketPrefix + name;
	}

	public static String getObjectPath(String bucket, String object) {
		return String.format("/%s/%s", bucket, object);
	}
}
