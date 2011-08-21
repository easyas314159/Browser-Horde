package com.browserhorde.server.api;

import javax.persistence.EntityManager;
import javax.persistence.Query;
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
import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.NoTasksResponse;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.util.Randomizer;
import com.google.inject.Inject;

@Path("workorders")
@Produces(MediaType.APPLICATION_JSON)
public class WorkorderResource {
	@Inject private Randomizer randomizer;
	@Inject private EntityManager entityManager;

	@GET
	public Response checkoutWorkorder(@Context SecurityContext sec) {
		return checkoutWorkorderForJob(sec, null);
	}

	@GET
	@Path("{id}")
	public Response checkoutWorkorderForJob(@Context SecurityContext sec, @PathParam("id") String id) {
		Job job = null;
		ApiResponse response = null;

		// TODO: Determine what kind of workorder we should be checking out depending on the user

		id = StringUtils.trimToNull(id);
		if(id == null) {
			String r = randomizer.nextRandomizer();

			Query le = entityManager.createQuery(
					"select * from " + Job.class.getName()
					+ " where randomizer <= :randomizer order by randomizer desc limit 1"
				);
			Query ge = entityManager.createQuery(
					"select * from " + Job.class.getName()
					+ " where randomizer >= :randomizer order by randomizer asc limit 1"
				);
			le.setParameter("randomizer", r);
			ge.setParameter("randomizer", r);

			if(job == null) {
				response = new NoTasksResponse();
			}
			else {
				job.setRandomizer(r);
				entityManager.merge(job);
			}
		}
		else {
			if(job == null) {
				response = new InvalidRequestResponse();
			}
		}

		if(response == null && job != null) {
			// TODO: Select random task
		}

		entityManager.close();

		return Response.ok(response).build();
	}

	private Task checkoutTask(String id) {
		return null;
	}

	@POST
	@Path("{id}")
	public Response checkinWorkorder(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
