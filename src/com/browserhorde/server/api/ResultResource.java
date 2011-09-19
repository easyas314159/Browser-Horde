package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.browserhorde.server.security.Roles;

@Path("results")
@Produces({MediaType.APPLICATION_JSON})
public class ResultResource {
	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listResults() {
		return Response.ok().build();
	}
}
