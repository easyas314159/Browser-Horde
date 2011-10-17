package com.browserhorde.server.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Provider;

public class GsonProvider implements Provider<Gson> {
	private final GsonBuilder gsonBuilder;

	public GsonProvider(GsonBuilder gsonBuilder) {
		this.gsonBuilder = gsonBuilder;
	}

	@Override
	public Gson get() {
		return gsonBuilder.create();
	}
}
