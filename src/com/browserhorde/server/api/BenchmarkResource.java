package com.browserhorde.server.api;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.google.inject.Inject;

@Path("benchmarks")
@Produces({MediaType.APPLICATION_JSON})
public class BenchmarkResource {
	@Inject private AmazonSQSAsync awsSQS;

	@GET
	public Response getBenchmark(@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId) {
		// TODO: See if we have an existing benchmark
		return Response.ok().build();
	}

	@POST
	public Response setBenchmark(@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId, String body) {
		return Response.status(ApiStatus.NO_CONTENT).build();
	}
}
