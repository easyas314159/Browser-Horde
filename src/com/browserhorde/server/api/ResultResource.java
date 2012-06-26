package com.browserhorde.server.api;

import javax.persistence.EntityManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.NotImplementedException;

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
	public Response getResult(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
	@DELETE
	@Path("{id}")
	public Response deleteResult(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/data")
	public Response getData(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
