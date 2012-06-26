package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.error.InvalidRequestException;
import com.google.gson.JsonNull;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public final class InvalidRequestExceptionMapper implements ExceptionMapper<InvalidRequestException> {
	@Override
	public Response toResponse(InvalidRequestException ex) {
		// TODO: This needs some meaningful body content
		return Response
			.status(Status.BAD_REQUEST)
			.entity(JsonNull.INSTANCE)
			.build();
	}
}