package com.browserhorde.server.api.readers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;

import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Injector;

@Consumes({MediaType.APPLICATION_JSON})
public class JsonReader<T> implements MessageBodyReader<T> {
	@Inject
	private Injector injector;

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] a, MediaType mediaType) {
		return true;
	}

	@Override
	public T readFrom(Class<T> clazz, Type type, Annotation[] a,
			MediaType mediaType, MultivaluedMap<String, String> headers,
			InputStream input) throws IOException, WebApplicationException {

		GsonBuilder builder = injector.getInstance(GsonBuilder.class);

		// TODO: Handle Character Encoding Properly
		try {
			Reader reader = new InputStreamReader(input);
			return builder.create().fromJson(reader, clazz);
		}
		catch(JsonSyntaxException ex) {
			throw new ApiException(ApiStatus.BAD_REQUEST, ex.getMessage());
		}
	}
}
