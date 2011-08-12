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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ApiResponseStatus;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.RequestDeniedResponse;
import com.browserhorde.server.entity.Job;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

@Path("jobs")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class JobResource {
	private final Logger log = Logger.getLogger(getClass());

	@Inject EntityManager entityManager;

	@GET
	public Response listJobs(@Context SecurityContext security) {
		ApiResponse response = null;

		// TODO: This needs to filter jobs by the user that owns them
		if(response == null) {

			Query query = entityManager.createQuery(
					"select * from " + Job.class.getName()
				);
			List<?> results = query.getResultList();
			response = new RequestDeniedResponse();
		}
		return Response.ok(response).build();
	}

	@GET
	@Path("{id}")
	public Response getJob(@PathParam("id") String id) {
		ApiResponse response = null;
		id = StringUtils.trimToNull(id);
		if(id == null) {
			response = new InvalidRequestResponse();
		}
		else {
			Job job = entityManager.find(Job.class, id);
			if(job == null) {
				return Response.status(Status.NOT_FOUND).build();
			}
			response = new ApiResponse(ApiResponseStatus.OK);
		}
		if(response == null) {
			response = new RequestDeniedResponse();
		}

		return Response.ok(response).build();
	}

	@POST
	public ApiResponse createJob(
			@FormParam("name") @QueryParam("name") @DefaultValue("New Job") String name,
			@FormParam("desc") @QueryParam("desc") String description,
			@FormParam("website") @QueryParam("website") String website,
			@FormParam("callback") @QueryParam("callback") String callback,
			@FormParam("public") @QueryParam("pulic") @DefaultValue("true") boolean ispublic,
			@FormParam("active") @QueryParam("active") @DefaultValue("false") boolean isactive,
			@FormParam("timeout") @QueryParam("timeout") Integer timeout
		) {

		Job job = new Job();
		job.setName(name);
		job.setDescription(description);
		job.setWebsite(website);
		job.setCallback(callback);
		job.setIspublic(ispublic);
		job.setIsactive(isactive);
		job.setTimeout(timeout);

		entityManager.persist(job);
		String id = job.getId();

		return new RequestDeniedResponse();
	}

	@PUT
	@Path("{id}")
	public ApiResponse updateJob(@PathParam("id") String id) {
		return new RequestDeniedResponse();
	}

	@DELETE
	@Path("{id}")
	public ApiResponse deleteJob(@PathParam("id") String id) {
		return new RequestDeniedResponse();
	}
}
