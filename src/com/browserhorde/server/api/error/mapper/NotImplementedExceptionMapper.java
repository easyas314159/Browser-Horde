package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import com.browserhorde.server.api.ApiStatus;
import com.google.gson.JsonNull;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class NotImplementedExceptionMapper implements ExceptionMapper<NotImplementedException> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Response toResponse(NotImplementedException ex) {
		log.warn("Not Implemented", ex);
		return Response
			.status(ApiStatus.NOT_IMPLEMENTED)
			.entity(new JsonNull())
			.build();
	}
}
