package com.browserhorde.server.aws;

public final class DomainManager {
	private static final String SUFFIX_USERS = "users";
	private static final String SUFFIX_JOBS = "jobs";
	private static final String SUFFIX_TASKS = "tasks";
	private static final String SUFFIX_RESULTS = "results";

	private static String domainPrefix = null;
	
	private DomainManager() {}

	public static void setDomainPrefix(String domainPrefix) {
		DomainManager.domainPrefix = domainPrefix;
	}
	public static String getDomainPrefix() {
		return DomainManager.domainPrefix;
	}

	public static String getDomain(String suffix) {
		return domainPrefix == null ? suffix : domainPrefix + suffix;
	}

	public static String getUsers() {
		return getDomain(SUFFIX_USERS);
	}
	public static String getJobs() {
		return getDomain(SUFFIX_JOBS);
	}
	public static String getTasks() {
		return getDomain(SUFFIX_TASKS);
	}
	public static String getResults() {
		return getDomain(SUFFIX_RESULTS);
	}
}
