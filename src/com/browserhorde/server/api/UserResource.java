package com.browserhorde.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("users")
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {
	@GET
	public Response getSelf(@Context SecurityContext sec) {
		return Response.ok(sec.getUserPrincipal()).build();
	}
}
