package com.browserhorde.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("tasks")
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource {
	@GET
	public Response listWorkorders() {
		return Response.ok().build();
	}
}
