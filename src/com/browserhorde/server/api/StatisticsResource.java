package com.browserhorde.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("stats")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class StatisticsResource {
	@GET
	public Response statistics() {
		return Response.ok().build();
	}
}
