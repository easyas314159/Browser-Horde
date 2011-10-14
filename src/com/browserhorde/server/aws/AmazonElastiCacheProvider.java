package com.browserhorde.server.aws;

import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheAsync;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonElastiCacheProvider implements Provider<AmazonElastiCache> {
	@Inject private AmazonElastiCacheAsync awsEC;

	@Override
	public AmazonElastiCache get() {
		return awsEC;
	}
}
