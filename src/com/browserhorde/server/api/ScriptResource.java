package com.browserhorde.server.api;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.consumes.ModifyScriptRequest;
import com.browserhorde.server.api.consumes.ScriptObject;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;

@Path("script")
@Produces({MediaType.APPLICATION_JSON})
public class ScriptResource {
	private static final Logger log = Logger.getLogger(getClass());

	@Inject private GsonBuilder gsonBuilder;

	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET) private String awsS3Bucket;
	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET_ENDPOINT) private String awsS3BucketEndpoint;

	private final EntityManager entityManager;

	private final AmazonS3 awsS3;

	@Inject
	public ScriptResource(EntityManager entityManager, AmazonS3 awsS3) {
		this.entityManager = entityManager;
		this.awsS3 = awsS3;
	}

	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listScripts(@Context SecurityContext sec, @QueryParam("q") @DefaultValue("") String search) {
		throw new NotImplementedException();
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.REGISTERED)
	public Response createScript(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			ScriptObject object
		) {
		User user = (User)sec.getUserPrincipal();

		Script script = new Script();

		script.setOwner(user);

		String name = null;
		if(object.containsKey(ScriptObject.NAME)) {
			name = StringUtils.trimToNull(object.getString(ScriptObject.NAME));
		}
		if(name == null) {
			name = "Untitled Script";
		}
		script.setName(name);

		if(object.containsKey(ScriptObject.DESCRIPTION)) {
			String desc = StringUtils.trimToNull(object.getString(ScriptObject.DESCRIPTION));
			script.setDescription(desc);
		}

		if(object.containsKey(ScriptObject.SHARED)) {
			Boolean shared = object.getBoolean();
			script.setShared(shared);
		}

		entityManager.persist(script);

		URI location = ui.getAbsolutePathBuilder()
			.path(getClass(), "getScript")
			.build(script.getId());
			;

		return Response
			.status(ApiStatus.CREATE)
			.location(location)
			.entity(script)
			.build()
			;
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

	@GET
	@Path("{id}/rev")
	public Response getRevisions(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@GET
	@Path("{id}/js")
	@Produces({"text/javascript", "application/javascript", "application/x-javascript"})
	public Response getSource(
			@Context HttpHeaders headers,
			@PathParam("id") String id,
			@QueryParam("d") @DefaultValue("false") Boolean debug,
			@QueryParam("v") @DefaultValue("") String version
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

	@POST
	@Path("{id}/js")
	@Consumes({"text/javascript", "application/javascript", "application/x-javascript"})
	@RolesAllowed(Roles.REGISTERED)
	public Response updateSource(@Context SecurityContext sec, @PathParam("id") String id, InputStream source) {
		User user = (User)sec.getUserPrincipal();

		Script script = entityManager.find(Script.class, id);
		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("application/javascript; charset=utf-8");

		PutObjectRequest putRequest = new PutObjectRequest(awsS3Bucket, id, source, metadata);
		PutObjectResult putResult;

		try {
			putResult = awsS3.putObject(putRequest);
			awsS3.setObjectAcl(awsS3Bucket, id, CannedAccessControlList.PublicRead);
		}
		catch(AmazonClientException ex) {
			throw new WebApplicationException(ex);
		}

		String versionId = putResult.getVersionId();

		/*
		script.setHeadVersion(versionId);
		ScriptVersion scriptVersion = new ScriptVersion(script.getId(), versionId);

		entityManager.merge(script);
		entityManager.persist(scriptVersion);
		*/

		return Response.status(ApiStatus.ACCEPTED).build();
	}

	/*
	private void storeScript(InputStream source, String bucket, String id) throws IOException {
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentType("application/javascript; charset=utf-8");

		PutObjectRequest putRequest = new PutObjectRequest(bucket, id, source, metadata); 

		awsS3.putObject(putRequest);
		awsS3.setObjectAcl(bucket, id, CannedAccessControlList.PublicRead);

		Gson gson = gsonBuilder.create();
	}
	*/

	private void populateScript(Script script, ScriptObject object) {
	}
}
