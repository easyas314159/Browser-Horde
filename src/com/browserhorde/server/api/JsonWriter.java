package com.browserhorde.server.api;

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

import com.browserhorde.server.util.GsonUtils;
import com.google.gson.Gson;

@Provider
@Produces(MediaType.APPLICATION_JSON)
public class JsonWriter implements MessageBodyWriter<Object> {

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

		Gson gson = GsonUtils.newGson();
		OutputStreamWriter writer = new OutputStreamWriter(output, "UTF-8");
		gson.toJson(response, type, writer);
		writer.flush();
	}

}
