package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.security.Roles;

@Path("users")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class UserResource {
	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response getSelf(@Context SecurityContext sec) {
		ApiResponse response = new ResourceResponse(sec.getUserPrincipal());
		return Response.ok(response).build();
	}
}
