package com.browserhorde.server.api.readers;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.fileupload.RequestContext;

import com.browserhorde.server.util.ParamUtils;

public class UploadContext implements RequestContext {
	private final MultivaluedMap<String, String> headers;
	private final InputStream input;

	public UploadContext(MultivaluedMap<String, String> headers, InputStream input) {
		this.headers = headers;
		this.input = input;
	}

	@Override
	public String getCharacterEncoding() {
		return headers.getFirst(HttpHeaders.CONTENT_ENCODING);
	}

	@Override
	public int getContentLength() {
		return ParamUtils.asInteger(headers.getFirst(HttpHeaders.CONTENT_LENGTH), 0);
	}

	@Override
	public String getContentType() {
		return headers.getFirst(HttpHeaders.CONTENT_TYPE);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return input;
	}		
}