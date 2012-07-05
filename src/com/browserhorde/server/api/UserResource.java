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
	private static final String ID_SELF = "me";

	private final EntityManager entityManager;
	private final Principal principal;

	@Inject
	public UserResource(EntityManager entityManager, Principal principal) {
		this.entityManager = entityManager;
		this.principal = principal;
	}

	@GET
	@RolesAllowed({Roles.REGISTERED})
	public Response listUsers(@QueryParam("q") @DefaultValue("") String search) {
		throw new NotImplementedException();
	}

	@POST
	@Path(ID_SELF)
	@RolesAllowed({Roles.REGISTERED})
	public Response updateSelf(UserObject object) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}")
	public Response getUser(@PathParam("id") String id) {
		if(StringUtils.equalsIgnoreCase(ID_SELF, id)) {
			id = ((User)principal).getId();
		}
		User user = entityManager.find(User.class, id);

		if(user == null) {
			throw new NotFoundException();
		}
		return Response
			.status(ApiStatus.OK)
			.entity(user)
			.build()
			;
	}

	@GET
	@Path("{id}/scripts")
	@RolesAllowed({Roles.REGISTERED})
	public Response getMyScripts(@PathParam("id") String id) {
		if(StringUtils.equalsIgnoreCase(ID_SELF, id)) {
			id = ((User)principal).getId();
		}
		User user = entityManager.find(User.class, id);

		if(user == null) {
			throw new NotFoundException();
		}

		return Response
			.status(ApiStatus.OK)
			.entity(user.getScripts())
			.build()
			;
	}

	@GET
	@Path("{id}/projects")
	@RolesAllowed({Roles.REGISTERED})
	public Response getMyProjects(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
