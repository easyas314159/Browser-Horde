package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.NotImplementedException;

import com.browserhorde.server.security.Roles;

import com.google.inject.Inject;

@Path("result")
@Produces({MediaType.APPLICATION_JSON})
public class ResultResource {
	private final EntityManager entityManager;

	@Inject
	public ResultResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@GET
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response getResult(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteResult(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/data")
	@RolesAllowed(Roles.REGISTERED)
	public Response getData(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
