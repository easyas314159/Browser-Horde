package com.browserhorde.server.api.readers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.fileupload.RequestContext;

@Provider
@Consumes({MediaType.MULTIPART_FORM_DATA})
public class RequestContextReader implements MessageBodyReader<RequestContext> {
	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] a, MediaType mediaType) {
		return mediaType.isCompatible(MediaType.MULTIPART_FORM_DATA_TYPE);
	}

	@Override
	public RequestContext readFrom(Class<RequestContext> clazz, Type type,
			Annotation[] a, MediaType mediaType,
			MultivaluedMap<String, String> headers, InputStream input)
			throws IOException, WebApplicationException {
		return new UploadContext(headers, input);
	}
}
