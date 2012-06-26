package com.browserhorde.server.api;

import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang.NotImplementedException;

import com.google.inject.Inject;

@Path("workorder")
@Produces({MediaType.APPLICATION_JSON})
public class WorkorderResource {
	private final EntityManager entityManager;

	@Inject
	public WorkorderResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@POST
	public Response createWorkorder(@Context SecurityContext sec) {
		throw new NotImplementedException();
	}
	@POST
	@Path("{id}")
	public Response submitWorkorder(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}")
	public Response cancelWorkorder(@Context SecurityContext sec, @PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
