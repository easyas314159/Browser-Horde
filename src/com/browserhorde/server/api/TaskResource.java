package com.browserhorde.server.api;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.entity.Task;
import com.google.inject.Inject;

@Path("tasks")
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource {
	private final Logger log = Logger.getLogger(getClass());

	@Inject EntityManager entityManager;

	// TODO: This should filter by job id
	@GET
	public Response listTasks() {
		ApiResponse response = null;

		if(response == null) {
			Query query = entityManager.createQuery(
					"select * from " + Task.class.getName()
				);
			List<?> results = query.getResultList();
			response = new ResourceResponse(results);
		}

		return Response.ok(response).build();
	}
}
