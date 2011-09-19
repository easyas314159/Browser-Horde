package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.ApiStatus;
import com.google.gson.JsonParseException;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {
	@Override
	public Response toResponse(JsonParseException ex) {
		return Response
			.status(ApiStatus.BAD_REQUEST)
			.entity("Mallformed JSON")
			.build();
	}
}
