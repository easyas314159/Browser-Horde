package com.browserhorde.server.gson;

import java.net.URI;
import java.net.URL;
import java.util.Date;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.inject.Provider;

public class GsonBuilderProvider implements Provider<GsonBuilder> {
	@Override
	public GsonBuilder get() {
		GsonBuilder gsonBuilder = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.registerTypeAdapter(Date.class, new DateHandler())
			.registerTypeHierarchyAdapter(JsonElement.class, new JsonElementHandler())
			.registerTypeHierarchyAdapter(URI.class, new URIHandler())
			.registerTypeHierarchyAdapter(URL.class, new URLHandler())
			;
		return gsonBuilder;
	}
}
