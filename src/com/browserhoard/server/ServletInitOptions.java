package com.browserhoard.server;

public final class ServletInitOptions {
	// Memcached
	public static final String MEMCACHED_CLUSTER = "memcached.cluster";
	public static final String MEMCACHED_CONNECTION_FACTORY = "memcached.connection_factory";

	// Amazon Web Services - General
	public static final String AWS_ACCESS_KEY = "aws.access_key";
	public static final String AWS_SECRET_KEY = "aws.secret_key";

	public static final String AWS_USER_AGENT = "aws.user_agent";

	// Amazon Web Services - S3
	public static final String AWS_S3_BUCKET = "aws.s3.bucket";

	// Amazon Web Services - SimpleDB
	public static final String AWS_SIMPLEDB_DOMAIN_PREFIX = "aws.simpledb.domain_prefix";
	public static final String AWS_SIMPLEDB_DOMAINS = "aws.simpledb.domains";

	private ServletInitOptions() {}
}
