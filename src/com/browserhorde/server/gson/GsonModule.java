package com.browserhorde.server.gson;

import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;

public class GsonModule extends AbstractModule {
	@Override
	protected void configure() {
		/*
		GsonBuilder gsonBuilder = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(Date.class, new DateHandler())
			.registerTypeHierarchyAdapter(JsonElement.class, new JsonElementHandler())
			.registerTypeHierarchyAdapter(URI.class, new URIHandler())
			.registerTypeHierarchyAdapter(URL.class, new URLHandler())
			;

		bind(GsonBuilder.class)
			.toInstance(gsonBuilder);
		*/
		
		bind(GsonBuilder.class)
			.toProvider(GsonBuilderProvider.class);
	}
}
