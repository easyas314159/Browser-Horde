package com.browserhorde.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("results")
@Produces(MediaType.APPLICATION_JSON)
public class ResultResource {
	@GET
	public Response listResults() {
		return Response.ok().build();
	}
}
