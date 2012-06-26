package com.browserhorde.server.api.error.mapper;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import com.browserhorde.server.api.ApiStatus;
import com.google.gson.JsonNull;

@Provider
@Produces({MediaType.APPLICATION_JSON})
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Response toResponse(WebApplicationException ex) {
		Response rsp = ex.getResponse();
		if(rsp.getStatus() == ApiStatus.FORBIDDEN.getStatusCode()) {
			return Response
				.status(ApiStatus.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"horde\"")
				.header(HttpHeaders.WWW_AUTHENTICATE, "OAuth")
				.entity(JsonNull.INSTANCE)
				.build();
		}
		log.error("Something Broke!", ex);
		return Response
			.status(ApiStatus.INTERNAL_SERVER_ERROR)
			.entity(JsonNull.INSTANCE)
			.build();
	}
}
