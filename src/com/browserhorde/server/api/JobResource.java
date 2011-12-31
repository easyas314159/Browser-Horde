package com.browserhorde.server.api;

import java.net.URI;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
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
import javax.ws.rs.core.UriInfo;

import com.browserhorde.server.api.consumes.ModifyJobRequest;
import com.browserhorde.server.api.error.ForbiddenException;
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
		Job job = entityManager.find(Job.class, id);
		if(job == null) {
			throw new NotFoundException();
		}

		return Response
			.status(ApiStatus.OK)
			.entity(job)
			.build()
			;
	}

	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createJob(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			ModifyJobRequest jobCreate
		) {
		User user = (User)sec.getUserPrincipal();
		Script script = entityManager.find(Script.class, jobCreate.script);

		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

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

		URI location = ui.getAbsolutePathBuilder()
			.path(job.getId())
			.build()
			;

		return Response
			.status(ApiStatus.CREATE)
			.location(location)
			.entity(job)
			.build()
			;
	}

	@POST
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateJob(
			@Context SecurityContext sec,
			@PathParam("id") String id,
			ModifyJobRequest modifyJob
		) {
		User user = (User)sec.getUserPrincipal();
		Job job = entityManager.find(Job.class, id);

		if(job == null || !job.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		if(!job.getScript().getId().equalsIgnoreCase(modifyJob.script)) {
			Script script = entityManager.find(Script.class, modifyJob.script);
			if(script != null && script.isOwnedBy(user)) {
				job.setScript(script);
			}
		}

		// TODO: This should perform some sort of patch instead of direct overwrite
		job.setName(modifyJob.name);
		job.setDescription(modifyJob.description);
		job.setWebsite(modifyJob.website);
		job.setCallback(modifyJob.callback);
		job.setActive(modifyJob.active);
		job.setTimeout(modifyJob.timeout);

		entityManager.merge(job);

		return Response
			.status(ApiStatus.OK)
			.entity(job)
			.build()
			;
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteJob(
			@Context SecurityContext sec,
			@PathParam("id") String id
		) {
		User user = (User)sec.getUserPrincipal();
		Job job = entityManager.find(Job.class, id);

		if(job == null || !job.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			entityManager.remove(job);

			// TODO: Delete any associated tasks
			// TODO: Delete task data and results
		}

		return Response.status(ApiStatus.NO_CONTENT).build();
	}
}
