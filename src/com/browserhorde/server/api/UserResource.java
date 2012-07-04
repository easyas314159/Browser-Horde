package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.NotImplementedException;

import com.browserhorde.server.api.consumes.ModifyUserRequest;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.google.inject.Inject;

@Path("user")
@Produces({MediaType.APPLICATION_JSON})
public class UserResource {
	private final EntityManager entityManager;

	@Inject
	public UserResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@GET
	@RolesAllowed({Roles.REGISTERED})
	public Response listUsers(@QueryParam("q") @DefaultValue("") String search) {
		throw new NotImplementedException();
	}

	@GET
	@Path("me")
	@RolesAllowed({Roles.REGISTERED})
	public Response getSelf(@Context SecurityContext sec) {
		User user = (User)sec.getUserPrincipal();

		return Response
			.status(ApiStatus.OK)
			.entity(user)
			.build()
			;
	}

	@POST
	@Path("me")
	@RolesAllowed({Roles.REGISTERED})
	public Response updateUser(
			@Context SecurityContext sec,
			ModifyUserRequest userModify
		) {

		throw new NotImplementedException();
	}

	@GET
	@Path("{id}")
	public Response getUserById(
			@Context SecurityContext sec,
			@PathParam("id") String id
		) {

		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/scripts")
	@RolesAllowed({Roles.REGISTERED})
	public Response getUserScripts(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/projects")
	@RolesAllowed({Roles.REGISTERED})
	public Response getUserProjects(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
