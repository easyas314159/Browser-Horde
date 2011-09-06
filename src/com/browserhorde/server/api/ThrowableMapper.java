package com.browserhorde.server.api;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ThrowableMapper implements ExceptionMapper<Throwable> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Response toResponse(Throwable ex) {
		log.warn("Uncaught Exception", ex);
		return Response
			.status(Status.INTERNAL_SERVER_ERROR)
			.build();
	}
}
