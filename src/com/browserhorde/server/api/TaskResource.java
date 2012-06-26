package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.NotImplementedException;

import com.browserhorde.server.security.Roles;
import com.google.inject.Inject;

@Path("tasks")
@Produces({MediaType.APPLICATION_JSON})
public class TaskResource {
	private final EntityManager entityManager;

	@Inject
	public TaskResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@GET
	@Path("{id}")
	public Response getTask(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@POST
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateTask(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteTask(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/workunits")
	@RolesAllowed(Roles.REGISTERED)
	public Response getWorkunits(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@POST
	@Path("{id}/workunits")
	@RolesAllowed(Roles.REGISTERED)
	public Response createWorkunit(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}/workunits")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteWorkunits(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
