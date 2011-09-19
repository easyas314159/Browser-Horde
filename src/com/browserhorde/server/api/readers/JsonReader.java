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

import com.browserhorde.server.util.GsonUtils;

@Consumes({MediaType.APPLICATION_JSON})
public class JsonReader<T> implements MessageBodyReader<T> {
	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] a, MediaType mediaType) {
		return true;
	}

	@Override
	public T readFrom(Class<T> clazz, Type type, Annotation[] a,
			MediaType mediaType, MultivaluedMap<String, String> headers,
			InputStream input) throws IOException, WebApplicationException {

		// TODO: Handle Character Encoding Properly
		Reader reader = new InputStreamReader(input);
		return GsonUtils.newGson().fromJson(reader, clazz);
	}
}
