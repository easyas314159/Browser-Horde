package com.browserhorde.server.gson;

import com.browserhorde.server.api.Api;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.servlet.RequestScoped;

public class GsonModule extends AbstractModule {
	@Override
	protected void configure() {
		bind(GsonBuilder.class)
			.annotatedWith(Api.class)
			.toProvider(GsonBuilderProvider.class)
			.in(RequestScoped.class);
	}
}
