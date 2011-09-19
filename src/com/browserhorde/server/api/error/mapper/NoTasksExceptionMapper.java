package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.error.NoTasksException;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public final class NoTasksExceptionMapper implements ExceptionMapper<NoTasksException> {
	@Override
	public Response toResponse(NoTasksException ex) {
		// TODO: This needs some meaningful body content
		return Response.ok().build();
	}
}