package com.browserhorde.server.api;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.api.exception.InvalidRequestException;
import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.browserhorde.server.util.Randomizer;
import com.google.inject.Inject;

@Path("jobs")
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class JobResource {
	@Inject EntityManager entityManager;
	@Inject Randomizer randomizer;

	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listJobs(@Context SecurityContext sec) {
		ApiResponse response = null;

		// TODO: This needs to filter jobs by the user that owns them
		if(response == null) {
			Query query = entityManager.createQuery(
					"select * from " + Job.class.getName()
					+ " where owner=:owner"
				);
			query.setParameter("owner", sec.getUserPrincipal());

			List<?> results = query.getResultList();
			response = new ResourceResponse(results);
		}
		return Response.ok(response).build();
	}

	@GET
	@Path("{id}")
	public Response getJob(@PathParam("id") String id) {
		ApiResponse response = null;

		if(response == null) {
			id = StringUtils.trimToNull(id);
			if(id == null) {
				throw new InvalidRequestException();
			}
			else {
				Job job = entityManager.find(Job.class, id);
				if(job == null) {
					return Response.status(Status.NOT_FOUND).build();
				}
				response = new ResourceResponse(job);
			}
		}

		return Response.ok(response).build();
	}

	// FIXME: For some reason we aren't picking up the form params
	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createJob(
			@Context SecurityContext sec,
			@FormParam("name") @DefaultValue("New Job") String name,
			@FormParam("desc") String description,
			@FormParam("website") String website,
			@FormParam("callback") String callback,
			@FormParam("script") String scriptId,
			@FormParam("public") @DefaultValue("true") boolean ispublic,
			@FormParam("active") @DefaultValue("false") boolean isactive,
			@FormParam("timeout") @DefaultValue("600") Integer timeout
		) {

		ApiResponse response = null;

		User user = (User)sec.getUserPrincipal();

		scriptId = StringUtils.trimToNull(scriptId);
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
				job.setRandomizer(randomizer.nextRandomizer());
				job.setName(name);
				job.setDescription(description);
				job.setWebsite(website);
				job.setCallback(callback);
				job.setIspublic(ispublic);
				job.setActive(isactive);
				job.setTimeout(timeout);
				job.setScript(script);
	
				// TODO: Check to make sure it successfully persisted
				entityManager.persist(job);
				response = new ResourceResponse(job);
			}
		}

		return Response.ok(response).build();
	}

	@PUT
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public ApiResponse updateJob(@PathParam("id") String id) {
		// TODO: If the user isn't logged in then request denied
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteJob(@PathParam("id") String id) {
		ApiResponse response = null;

		// TODO: If the user isn't logged in then request denied
		if(response == null) {
			Job job = entityManager.find(Job.class, id);
			if(job == null) {
				throw new InvalidRequestException();
			}
			else {
				entityManager.remove(job);
				//response = new ApiResponse(ApiResponseStatus.OK);
			}
		}

		// TODO: Delete any associated tasks
		return Response.ok(response).build();
	}
}
