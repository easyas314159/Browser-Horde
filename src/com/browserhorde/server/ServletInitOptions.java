package com.browserhorde.server;

public final class ServletInitOptions {
	private ServletInitOptions() {}

	// Persistence
	public static final String JPA_UNIT = "JPA_UNIT";

	// Dependency Injection
	public static final String GUICE_STAGE = "GUICE_STAGE";

	// Rate Limiting
	public static final String RATE_LIMIT = "HORDE_RATE_LIMIT";
	public static final String RATE_LIMIT_TIMEOUT = "HORDER_RATE_LIMIT_TIMEOUT";

	// Thread Pool
	public static final String EXECUTOR_CORE_POOL_SIZE = "HORDE_EXECUTOR_CORE_POOL_SIZE"; 
	public static final String EXECUTOR_KEEP_ALIVE_TIMEOUT = "HORDE_EXECUTOR_KEEP_ALIVE_TIMEOUT";
	public static final String EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT = "HORDE_EXECUTOR_ALLOW_CORE_THREAD_TIMEOUT";
	public static final String EXECUTOR_MAX_POOL_SIZE = "HORDE_EXECUTOR_MAX_POOL_SIZE";

	// Memcached
	public static final String MEMCACHED_CLUSTER = "HORDE_MEMCACHED_CLUSTER";
	public static final String MEMCACHED_FAILURE_MODE = "HORDE_MEMCACHED_FAILURE_MODE";
	public static final String MEMCACHED_PROTOCOL = "HORDE_MEMCACHED_PROTOCOL";

	// Stripe
	public static final String STRIPE_API_KEY = "STRIPE_API_KEY";

	// Amazon Web Services - General
	public static final String AWS_ACCESS_KEY = "AWS_ACCESS_KEY_ID";
	public static final String AWS_SECRET_KEY = "AWS_SECRET_KEY";

	public static final String USER_AGENT = "HORDE_USER_AGENT";

	// Amazon Web Services - S3
	public static final String AWS_S3_BUCKET = "HORDE_AWS_S3_BUCKET";
	public static final String AWS_S3_BUCKET_ENDPOINT = "HORDE_AWS_S3_BUCKET_ENDPOINT";

	// Amazon Web Services - SimpleDB
	public static final String AWS_SDB_DOMAIN_PREFIX = "HORDE_AWS_SDB_DOMAIN_PREFIX";

	// Amazon Web Services - SES
	public static final String AWS_SES_SENDER = "HORDE_AWS_SES_SENDER";

	// Amazon Web Services - SQS
	public static final String AWS_SQS_PREFIX = "HORDE_AWS_SQS_PREFIX";
}
