package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
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

import com.browserhorde.server.security.Roles;
import com.google.inject.Inject;

@Path("project")
@Produces({MediaType.APPLICATION_JSON})
public class ProjectResource {
	private final EntityManager entityManager;

	@Inject
	public ProjectResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@GET
	public Response listProjects(@Context SecurityContext sec, @QueryParam("q") @DefaultValue("") String search) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}	
	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createProject(@Context SecurityContext sec) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}

	@GET
	@Path("{id}")
	public Response getProject(@Context SecurityContext sec, @PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}
	@POST
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateProject(@PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}
	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteProject(@PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}

	@GET
	@Path("{id}/tasks")
	@RolesAllowed(Roles.REGISTERED)
	public Response listTasks(@Context SecurityContext sec, @PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}
	@POST
	@Path("{id}/tasks")
	@RolesAllowed(Roles.REGISTERED)
	public Response createTask(@Context SecurityContext sec, @PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}
	@DELETE
	@Path("{id}/tasks")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteTasks(@Context SecurityContext sec, @PathParam("id") String id) {
		return Response.status(ApiStatus.NOT_IMPLEMENTED).build();
	}
}
