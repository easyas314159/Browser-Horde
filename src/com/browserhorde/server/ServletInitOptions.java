package com.browserhorde.server;

public final class ServletInitOptions {
	// Thread Pool
	public static final String EXECUTOR_CORE_POOL_SIZE = "executor.core_pool_size"; 
	public static final String EXECUTOR_CORE_THREAD_TIMEOUT = "executor.core_thread_timeout";
	public static final String EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT = "executor.allow_core_thread_timeout";
	public static final String EXECUTOR_MAX_POOL_SIZE = "executor.max_pool_size";

	// Memcached
	public static final String MEMCACHED_CLUSTER = "memcached.cluster";
	public static final String MEMCACHED_CONNECTION_FACTORY = "memcached.connection_factory";

	// Amazon Web Services - General
	public static final String AWS_ACCESS_KEY = "aws.access_key";
	public static final String AWS_SECRET_KEY = "aws.secret_key";

	public static final String AWS_OBJECT_PREFIX = "aws.object_prefix";
	public static final String AWS_USER_AGENT = "aws.user_agent";

	// Amazon Web Services - S3
	public static final String AWS_S3_BUCKET_PREFIX = AWS_OBJECT_PREFIX;
	public static final String AWS_S3_BUCKETS = "aws.s3.buckets";

	// Amazon Web Services - SimpleDB
	public static final String AWS_SIMPLEDB_DOMAIN_PREFIX = AWS_OBJECT_PREFIX;
	public static final String AWS_SIMPLEDB_DOMAINS = "aws.simpledb.domains";

	private ServletInitOptions() {}
}
