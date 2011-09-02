package com.browserhorde.server.inject;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class AmazonSimpleDBProvider implements Provider<AmazonSimpleDB> {
	@Inject private AmazonSimpleDBAsync awsSDB;

	@Override
	public AmazonSimpleDB get() {
		return awsSDB;
	}
}
