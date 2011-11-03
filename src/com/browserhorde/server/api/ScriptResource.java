package com.browserhorde.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.consumes.ModifyScriptRequest;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.inject.QueueGZIP;
import com.browserhorde.server.inject.QueueMinify;
import com.browserhorde.server.queue.ProcessObject;
import com.browserhorde.server.security.Roles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;

@Path("scripts")
@Produces({MediaType.APPLICATION_JSON})
public class ScriptResource {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private GsonBuilder gsonBuilder;
	
	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET) private String awsS3Bucket;
	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET_ENDPOINT) private String awsS3BucketEndpoint;

	private final String awsSqsGzip;
	private final String awsSqsMinify;

	@Inject private EntityManager entityManager;

	@Inject private AmazonS3 awsS3;
	@Inject private AmazonSQSAsync awsSQS;

	@Inject
	public ScriptResource(@QueueGZIP String gzipQueue, @QueueMinify String minQueue) {
		this.awsSqsGzip = gzipQueue;
		this.awsSqsMinify = minQueue;
	}

	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listScripts(@Context SecurityContext sec) {
		Object entity = null;

		// TODO: This needs to handle some pagination
		Query query = entityManager.createQuery(
				"SELECT * FROM " + Script.class.getName()
				+ " WHERE owner=:owner"
			);
		query.setParameter("owner", sec.getUserPrincipal());

		entity = query.getResultList();

		return Response.ok(entity).build();
	}

	@GET
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response getScript(@Context SecurityContext sec, @PathParam("id") String id) {
		User user = (User)sec.getUserPrincipal();

		// TODO: IS this ownership check necessary?
		Script script = entityManager.find(Script.class, id);
		if(script == null || !script.isOwnedBy(user)) {
			throw new NotFoundException();
		}

		return Response
			.status(ApiStatus.OK)
			.entity(script)
			.build()
			;
	}

	@GET
	@Path("{id}.js")
	@Produces({"text/javascript", "application/javascript", "application/x-javascript"})
	public Response getScriptContent(
			@Context HttpHeaders headers,
			@PathParam("id") String id,
			@QueryParam("debug") @DefaultValue("false") Boolean debug
		) {
		Script script = entityManager.find(Script.class, id);
		if(script == null) {
			throw new NotFoundException();
		}

		Set<String> acceptedEncodings = new HashSet<String>();
		List<String> acceptHeaders = headers.getRequestHeader(HttpHeaders.ACCEPT_ENCODING);
		if(acceptHeaders != null) {
			for(String acceptHeader : acceptHeaders) {
				acceptHeader = acceptHeader.trim();
				String encodings[] = acceptHeader.split("\\s*,\\s*");
				for(String encoding : encodings) {
					String details[] = encoding.split("\\s*(;|=)\\s*", 3);
					if(details.length == 1) {
						acceptedEncodings.add(details[0]);
					}
					else if(details.length == 3 && details[1].equals("q") && details[2].matches("(0(\\.\\d{1,3})?)|(1(\\.0{1,3})?)")) {
						Float weight = Float.valueOf(details[2]);
						if(weight > 0.0f) {
							acceptedEncodings.add(details[0]);
						}
						else {
							acceptedEncodings.remove(details[0]);
						}
					}
					else {
						log.debug(String.format("Bad Accept-Encoding header \'%s\'", encoding));
					}
				}
			}
		}

		String key = script.getId();
		if(!debug) {
			key += ".min";
		}
		if(acceptedEncodings.contains("gzip")) {
			key += ".gz";
		}

		try {
			URI uriS3 = URIUtils.createURI("http", awsS3BucketEndpoint, -1, key, null, null);
			return Response
				.status(ApiStatus.FOUND)
				.location(uriS3)
				.build()
				;
		}
		catch(URISyntaxException ex) {
			throw new WebApplicationException(ex);
		}
	}

	// TODO: Refine file upload because multipart/form-data sucks
	/*
	New upload strategy:
		-POST to create
			-Create script object in SDB
			-Create empty script file in S3
		-PUT script contents
			-Receive script and queue for processing
			-return 202 ACCEPTED
		-GET needs to return script version info and processing status info
	*/

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.REGISTERED)
	public Response createScript(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			ModifyScriptRequest modifyScript
		) {
		User user = (User)sec.getUserPrincipal();
		
		Script script = new Script();

		script.setOwner(user);
		script.setName(modifyScript.name);
		script.setDescription(modifyScript.description);

		entityManager.persist(script);

		URI location = ui.getAbsolutePathBuilder()
			.path(script.getId())
			.build()
			;

		return Response
			.status(ApiStatus.CREATE)
			.location(location)
			.entity(script)
			.build()
			;
	}

	@POST
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.REGISTERED)
	public Response updateScript(
			@Context SecurityContext sec,
			@PathParam("id") String id,
			ModifyScriptRequest modifyScript
		) {
		User user = (User)sec.getUserPrincipal();

		Script script = entityManager.find(Script.class, id);
		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		// TODO: This should perform some sort of patch instead of direct overwrite
		script.setName(modifyScript.name);
		script.setDescription(modifyScript.description);

		entityManager.merge(script);

		return Response
			.status(ApiStatus.OK)
			.entity(script)
			.build()
			;
	}

	@PUT
	@Path("{id}")
	@Consumes({"text/javascript", "application/javascript", "application/x-javascript"})
	@RolesAllowed(Roles.REGISTERED)
	public Response storeScript(@Context SecurityContext sec, @PathParam("id") String id, InputStream source) {
		User user = (User)sec.getUserPrincipal();

		Script script = entityManager.find(Script.class, id);
		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			try {
				storeScript(source, awsS3Bucket, script.getId());
			}
			catch(IOException ex) {
				throw new WebApplicationException(ex);
			}
		}

		return Response.status(ApiStatus.ACCEPTED).build();
	}

	@DELETE
	@Path("{id}")
	public Response deleteScript(@Context SecurityContext sec, @PathParam("id") String id) {
		User user = (User)sec.getUserPrincipal();
		Script script = entityManager.find(Script.class, id);

		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			// TODO: Only delete scripts with no dependencies on other jobs
			// TODO: Do all of this either asynchronously or on a completely different server

			ListObjectsRequest listObjects = new ListObjectsRequest()
				.withBucketName(awsS3Bucket)
				.withPrefix(script.getId());
			while(true) {
				ObjectListing listing = awsS3.listObjects(listObjects);

				for(S3ObjectSummary summary : listing.getObjectSummaries()) {
					awsS3.deleteObject(awsS3Bucket, summary.getKey());
				}

				String marker = listing.getMarker();
				if(listing.isTruncated() || marker == null) {
					break;
				}
				listObjects.setMarker(marker);
			}
			entityManager.remove(script);
		}

		return Response.status(ApiStatus.NO_CONTENT).build();
	}

	private void storeScript(InputStream source, String bucket, String id) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("application/javascript; charset=utf-8");

		PutObjectRequest putRequest = new PutObjectRequest(bucket, id, source, metadata); 

		awsS3.putObject(putRequest);
		awsS3.setObjectAcl(bucket, id, CannedAccessControlList.PublicRead);

		Gson gson = gsonBuilder.create();

		ProcessObject gz = new ProcessObject(bucket, id);
		awsSQS.sendMessageAsync(new SendMessageRequest(awsSqsGzip, gson.toJson(gz)));

		ProcessObject min = new ProcessObject(bucket, id);
		awsSQS.sendMessageAsync(new SendMessageRequest(awsSqsMinify, gson.toJson(min)));
	}
}
