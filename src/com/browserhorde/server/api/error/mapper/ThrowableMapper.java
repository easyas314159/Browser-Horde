package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.google.gson.JsonNull;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class ThrowableMapper implements ExceptionMapper<Throwable> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Response toResponse(Throwable t) {
		log.error("Something Broke!", t);
		return Response
			.status(Status.INTERNAL_SERVER_ERROR)
			.entity(JsonNull.INSTANCE)
			.build();
	}
}
