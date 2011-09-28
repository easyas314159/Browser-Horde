package com.browserhorde.server.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.entity.User;

@Path("")
public class RootResource {
	private final Logger log = Logger.getLogger(getClass());

	@GET
	public Response serviceDiscovery(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			@HeaderParam(HttpHeaders.IF_NONE_MATCH) String etag
		) {
		List<String> links = new Vector<String>();
		User user = (User)sec.getUserPrincipal();
		if(user == null) {
		}
		else {
		}

		links.add(link(
				ui.getBaseUriBuilder().path(BenchmarkResource.class).build(),
				"benchmarks", null, null, null, null
			));
		links.add(link(
				ui.getBaseUriBuilder().path(WorkorderResource.class).build(),
				"workorders", null, null, null, null
			));

		ResponseBuilder responseBuilder = Response.status(ApiStatus.OK);
		for(String link : links) {
			responseBuilder.header("Link", link);
		}

		return responseBuilder.build();
	}

	private String link(URI uri, String rel, String rev, String title, URI anchor, Map<String, String> extensions) {
		List<String> link = new ArrayList<String>();
		link.add(String.format("<%s>", uri.toString()));

		link.add(link("rel", rel));
		link.add(link("rev", rev));
		link.add(link("title", title));
		link.add(link("anchor", anchor));

		if(extensions != null) {
			for(Map.Entry<String, String> ext : extensions.entrySet()) {
				link.add(link(ext.getKey(), ext.getValue()));
			}
		}

		while(link.remove(null));

		return StringUtils.join(link, ';');
	}
	
	private String link(String token, String value) {
		value = StringUtils.trimToNull(value);
		if(value == null) {
			return null;
		}
		return String.format("%s=\"%s\"", token, value);
	}
	private String link(String token, URI value) {
		if(value == null)  {
			return null;
		}
		return String.format("%s=\"%s\"", token, value);
	}
}
