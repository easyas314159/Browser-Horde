package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.ApiStatus;
import com.browserhorde.server.api.error.ForbiddenException;
import com.google.gson.JsonNull;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public final class ForbiddenExceptionMapper implements ExceptionMapper<ForbiddenException> {
	@Override
	public Response toResponse(ForbiddenException ex) {
		return Response
			.status(ApiStatus.FORBIDDEN)
			.entity(new JsonNull())
			.build()
			;
	}
}