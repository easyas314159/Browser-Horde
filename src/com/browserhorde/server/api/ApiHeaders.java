package com.browserhorde.server.api;

public final class ApiHeaders {
	public static final String X_HORDE_MACHINE_ID = "X-Horde-Machine-Id";
	public static final String X_HORDE_BENCHMARK = "X-Horde-Benchmark";

	public static final String X_RATE_LIMIT = "X-RateLimit";
	public static final String X_RATE_LIMIT_REMAINING = "X-RateLimit-Remaning";
	public static final String X_RATE_LIMIT_RESET = "X-RateLimit-Reset";
	
	public static final String X_FORWARDED_FOR = "X-Forwarded-For";

	private ApiHeaders() {}
}
