package com.browserhorde.server.api;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.NotImplementedException;

import com.google.inject.Inject;

@Path("workorders")
@Produces(MediaType.APPLICATION_JSON)
public class WorkorderResource {
	@Inject private MemcachedClient memcached;

	@POST
	public Response checkoutWorkorder() {
		throw new NotImplementedException();
	}
	@POST
	@Path("{id}")
	public Response checkinWorkorder(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
