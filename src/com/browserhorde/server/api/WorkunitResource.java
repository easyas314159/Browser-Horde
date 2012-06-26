package com.browserhorde.server.api;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;

import com.browserhorde.server.security.Roles;
import com.google.gson.JsonElement;
import com.google.inject.Inject;

@Path("workunit")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class WorkunitResource {
	private final EntityManager entityManager;

	@Inject
	public WorkunitResource(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@GET
	@Path("{id}")
	public Response getWorkunit(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@POST
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateWorkunit(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteWorkunit(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/data")
	public Response getData(@PathParam("id") String id) {
		throw new NotImplementedException();

		/*
		if(userAgent.indexOf("AppleWebKit") < 0) {
			302 Redirect
		}
		// HACK: This works around webkit bug 57600
		// https://bugs.webkit.org/show_bug.cgi?id=57600
		else {
			Proxy data to the client
		}
		*/
	}
	@POST
	@Path("{id}/data}")
	@RolesAllowed(Roles.REGISTERED)
	@Consumes({MediaType.APPLICATION_JSON})
	public Response updateData(@PathParam("id") String id, JsonElement data) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/results")
	@RolesAllowed(Roles.REGISTERED)
	public Response getResults(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}/results")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteResults(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
