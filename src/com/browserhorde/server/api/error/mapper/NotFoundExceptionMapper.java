package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.browserhorde.server.api.ApiStatus;
import com.google.gson.JsonNull;
import com.sun.jersey.api.NotFoundException;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public final class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Response toResponse(NotFoundException ex) {
		log.info("Not Found", ex);
		return Response
			.status(ApiStatus.NOT_FOUND)
			.entity(JsonNull.INSTANCE)
			.build()
			;
	}
}