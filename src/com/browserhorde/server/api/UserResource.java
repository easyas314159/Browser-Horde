package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.NotImplementedException;

import com.browserhorde.server.api.consumes.ModifyUserRequest;
import com.browserhorde.server.security.Roles;

@Path("users")
@Produces({MediaType.APPLICATION_JSON})
public class UserResource {
	@GET
	@RolesAllowed({Roles.REGISTERED})
	public Response getSelf(@Context SecurityContext sec) {
		Object entity = sec.getUserPrincipal();

		return Response
			.status(ApiStatus.OK)
			.entity(entity)
			.build()
			;
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed({Roles.REGISTERED})
	public Response updateUser(
			@Context SecurityContext sec,
			ModifyUserRequest userModify
		) {

		throw new NotImplementedException();
	}
}
