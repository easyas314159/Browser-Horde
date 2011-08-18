package com.browserhorde.server;

public final class ServletInitOptions {
	private ServletInitOptions() {}

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

	public static final String AWS_USER_AGENT = "aws.user_agent";

	// Amazon Web Services - S3
	public static final String AWS_S3_BUCKET = "aws.s3.bucket";

	// Amazon Web Services - SimpleDB
	public static final String AWS_SIMPLEDB_DOMAIN_PREFIX = "aws.simpledb.domain_prefix";
	
	// Amazon Web Services - SES
	public static final String AWS_SES_SENDER = "aws.ses.sender";
}
