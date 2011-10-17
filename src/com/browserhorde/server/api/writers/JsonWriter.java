package com.browserhorde.server.api.writers;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.Api;
import com.browserhorde.server.gson.Visibility;
import com.browserhorde.server.gson.VisibilityExclusionStrategy;
import com.browserhorde.server.gson.VisibilityLevel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonWriter implements MessageBodyWriter<Object> {
	private final Injector injector;

	private ThreadLocal<GsonBuilder> threadLocalBuilders;

	@Inject
	public JsonWriter(Injector injector) {
		this.injector = injector;

		threadLocalBuilders = new ThreadLocal<GsonBuilder>();
	}

	@Override
	public long getSize(Object response, Class<?> clazz, Type type, Annotation[] a, MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> clazz, Type type, Annotation[] a, MediaType mediaType) {
		return Object.class.isAssignableFrom(clazz);
	}

	@Override
	public void writeTo(Object response, Class<?> clazz, Type type,
			Annotation[] a, MediaType mediaType,
			MultivaluedMap<String, Object> headers, OutputStream output)
			throws IOException, WebApplicationException {

		Visibility visibility = null;
		for(Annotation annot : a) {
			if(annot instanceof Visibility) {
				visibility = (Visibility)annot;
				break;
			}
		}

		// TODO: If the user is logged in visibility should probably be PRIVATE
		VisibilityLevel level = visibility == null ? VisibilityLevel.PUBLIC : visibility.value();

		Gson gson = getBuilder().setExclusionStrategies(new VisibilityExclusionStrategy(level)).create();
		OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
		gson.toJson(response, type, writer);
		writer.flush();
	}
	
	private GsonBuilder getBuilder() {
		return injector.getInstance(Key.get(GsonBuilder.class, Api.class));
	}
}
