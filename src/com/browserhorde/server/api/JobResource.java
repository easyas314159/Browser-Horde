package com.browserhorde.server.api;

import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.api.consumes.ModifyJobRequest;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.api.error.InvalidRequestException;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

@Path("jobs")
@Produces({MediaType.APPLICATION_JSON})
public class JobResource {
	@Inject EntityManager entityManager;

	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listJobs(@Context SecurityContext sec) {
		Object entity = null;

		// TODO: This needs some pagination
		Query query = entityManager.createQuery(
				"select * from " + Job.class.getName()
				+ " where owner=:owner"
			);
		query.setParameter("owner", sec.getUserPrincipal());

		entity = query.getResultList();

		return Response.ok(entity).build();
	}

	@GET
	@Path("{id}")
	public Response getJob(@PathParam("id") String id) {
		Object response = null;

		if(response == null) {
			id = StringUtils.trimToNull(id);
			if(id == null) {
				throw new InvalidRequestException();
			}
			else {
				Job job = entityManager.find(Job.class, id);
				if(job == null) {
					throw new NotFoundException();
				}
				response = job;
			}
		}

		return Response.ok(response).build();
	}

	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createJob(
			@Context SecurityContext sec,
			ModifyJobRequest jobCreate
		) {

		Object response = null;

		User user = (User)sec.getUserPrincipal();

		String scriptId = StringUtils.trimToNull(jobCreate.script);
		if(scriptId == null) {
			throw new InvalidRequestException();
		}
		else {
			Script script = entityManager.find(Script.class, scriptId);
			if(script == null) {
				throw new InvalidRequestException();
			}
			else {
				// TODO: Some input validation here would be nice
				Job job = new Job();
	
				job.setOwner(user);
				job.setRandomizer(UUID.randomUUID().toString());
				job.setName(jobCreate.name);
				job.setDescription(jobCreate.description);
				job.setWebsite(jobCreate.website);
				job.setCallback(jobCreate.callback);
				job.setActive(jobCreate.active);
				job.setTimeout(jobCreate.timeout);
				job.setScript(script);
	
				// TODO: Check to make sure it successfully persisted
				entityManager.persist(job);
				response = job;
			}
		}

		return Response.ok(response).build();
	}

	@PUT
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateJob(
			@Context SecurityContext sec,
			@PathParam("id") String id,
			ModifyJobRequest jobUpdate
		) {
		User user = (User)sec.getUserPrincipal();
		Job job = entityManager.find(Job.class, id);

		if(job == null) {
			// TODO: Job doesn't exist
		}
		else if(!job.isOwnedBy(user)) {
			// TODO: Job isn't owned by this user
		}
		else {
			// TODO: Update job
		}

		return Response.ok().build();
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteJob(
			@Context SecurityContext sec,
			@PathParam("id") String id
		) {
		Object response = null;

		User user = (User)sec.getUserPrincipal();
		if(response == null) {
			Job job = entityManager.find(Job.class, id);
			if(job == null || !job.isOwnedBy(user)) {
				throw new ForbiddenException();
			}
			else {
				entityManager.remove(job);
				// TODO: Delete any associated tasks
			}
		}

		return Response.status(ApiStatus.NO_CONTENT).build();
	}
}
