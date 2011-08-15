package com.browserhorde.server.api;

import java.util.List;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ApiResponseStatus;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Script;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@Path("jobs")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {
	private final Logger log = Logger.getLogger(getClass());

	@Inject EntityManager entityManager;

	@GET
	public Response listJobs() {
		ApiResponse response = null;

		// TODO: This needs to filter jobs by the user that owns them
		if(response == null) {
			Query query = entityManager.createQuery(
					"select * from " + Job.class.getName()
				);
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
				response = new InvalidRequestResponse();
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
	public Response createJob(
			@FormParam("name") @QueryParam("name") @DefaultValue("New Job") String name,
			@FormParam("desc") @QueryParam("desc") String description,
			@FormParam("website") @QueryParam("website") String website,
			@FormParam("callback") @QueryParam("callback") String callback,
			@FormParam("script") @QueryParam("script") String scriptId,
			@FormParam("public") @QueryParam("pulic") @DefaultValue("true") boolean ispublic,
			@FormParam("active") @QueryParam("active") @DefaultValue("false") boolean isactive,
			@FormParam("timeout") @QueryParam("timeout") Integer timeout
		) {

		ApiResponse response = null;

		// TODO: If the user isn't logged in then request denied
		// response = new RequestDeniedResponse()

		Script script = entityManager.find(Script.class, scriptId);
		if(script == null) {
			response = new InvalidRequestResponse();
		}
		else {
			// TODO: Some input validation here would be nice
			Job job = new Job();
			job.setName(name);
			job.setDescription(description);
			job.setWebsite(website);
			job.setCallback(callback);
			job.setIspublic(ispublic);
			job.setIsactive(isactive);
			job.setTimeout(timeout);
			job.setScript(script);

			// TODO: Check to make sure it successfully persisted
			entityManager.persist(job);
			response = new ResourceResponse(job);
		}

		return Response.ok(response).build();
	}

	@PUT
	@Path("{id}")
	public ApiResponse updateJob(@PathParam("id") String id) {
		// TODO: If the user isn't logged in then request denied
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	public Response deleteJob(@PathParam("id") String id) {
		ApiResponse response = null;

		// TODO: If the user isn't logged in then request denied
		if(response == null) {
			Job job = entityManager.find(Job.class, id);
			if(job == null) {
				response = new InvalidRequestResponse();
			}
			else {
				entityManager.remove(job);
				response = new ApiResponse(ApiResponseStatus.OK);
			}
		}
		return Response.ok(response).build();
	}
}
