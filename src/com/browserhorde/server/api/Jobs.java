package com.browserhorde.server.api;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.RequestDeniedResponse;
import com.google.inject.Inject;

@Path("jobs")
@Produces(MediaType.APPLICATION_JSON)
public class Jobs {
	@Inject private AmazonSimpleDB awsSDB;

	@GET
	public ApiResponse listJobs(@Context SecurityContext security) {
		return new RequestDeniedResponse();
	}

	@GET
	@Path("{id}")
	public ApiResponse getJob(@PathParam("id") String id) {
		id = StringUtils.trimToNull(id);
		if(id == null) {
			return new InvalidRequestResponse();
		}

		return new RequestDeniedResponse();
	}

	@POST
	public ApiResponse createJob() {
		// TODO: Set location of new resource

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
