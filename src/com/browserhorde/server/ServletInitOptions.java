package com.browserhorde.server;

public final class ServletInitOptions {
	private ServletInitOptions() {}

	// Thread Pool
	public static final String EXECUTOR_CORE_POOL_SIZE = "HORDE_EXECUTOR_CORE_POOL_SIZE"; 
	public static final String EXECUTOR_KEEP_ALIVE_TIMEOUT = "HORDE_EXECUTOR_KEEP_ALIVE_TIMEOUT";
	public static final String EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT = "HORDE_EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT";
	public static final String EXECUTOR_MAX_POOL_SIZE = "HORDE_EXECUTOR_MAX_POOL_SIZE";

	// Memcached
	public static final String MEMCACHED_CLUSTER = "HORDE_MEMCACHED_CLUSTER";
	public static final String MEMCACHED_CLUSTER_ID = "HORDE_MEMCACHED_CLUSTER_ID";
	public static final String MEMCACHED_CONNECTION_FACTORY = "HORDE_MEMCACHED_CONNECTION_FACTORY";

	// Amazon Web Services - General
	public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
	public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";

	public static final String USER_AGENT = "HORDE_USER_AGENT";

	// Amazon Web Services - S3
	public static final String AWS_S3_BUCKET = "HORDE_AWS_S3_BUCKET";
	public static final String AWS_S3_BUCKET_ENDPOINT = "HORDE_AWS_S3_BUCKET_ENDPOINT";
	public static final String AWS_S3_PROXY = "HORDE_AWS_S3_PROXY";

	// Amazon Web Services - SimpleDB
	public static final String AWS_SDB_DOMAIN_PREFIX = "HORDE_AWS_SDB_DOMAIN_PREFIX";

	// Amazon Web Services - SES
	public static final String AWS_SES_SENDER = "HORDE_AWS_SES_SENDER";
}
