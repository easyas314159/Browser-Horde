package com.browserhorde.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.consumes.ModifyScriptRequest;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.aws.AmazonS3AsyncPutObject;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.browserhorde.server.util.ConcurrentPipeStream;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

@Path("scripts")
@Produces({MediaType.APPLICATION_JSON})
public class ScriptResource {
	private static final String BASE_PATH = "scripts";

	private static final String FILE_ORIGINAL = ".js";
	private static final String FILE_MINIFIED = "min.js";
	private static final String FILE_COMPRESSED = ".js.gz";
	private static final String FILE_MINIFIED_COMPRESSED = "min.js.gz";

	private final Logger log = Logger.getLogger(getClass());

	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET) private String awsS3Bucket;
	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET_ENDPOINT) private String awsS3BucketEndpoint;
	@Inject @Named(ServletInitOptions.AWS_S3_PROXY) private Boolean awsS3Proxy;

	@Inject private EntityManager entityManager;
	@Inject private ExecutorService executorService;

	@Inject private AmazonS3 awsS3;

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
			@PathParam("id") String id
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

		boolean mini = !script.isDebug();
		boolean gzip = acceptedEncodings.contains("gzip");

		String key = getObjectKey(
				script.getId(),
				gzip ? (mini ? FILE_MINIFIED_COMPRESSED : FILE_COMPRESSED) : (mini ? FILE_MINIFIED : FILE_ORIGINAL)
			);

		ResponseBuilder responseBuilder;
		if(awsS3Proxy) {
			responseBuilder = Response.status(ApiStatus.OK);
			S3Object object = awsS3.getObject(awsS3Bucket, key);
			responseBuilder
				.entity(object.getObjectContent())
				;
			if(gzip) {
				responseBuilder.header("Content-Encoding", "gzip");
			}
		}
		else {
			try {
				responseBuilder = Response.status(ApiStatus.TEMPORARY_REDIRECT);
				// TODO: If we are using cloud front then we need to be able to change the domain
				URI uriS3 = URIUtils.createURI("http", awsS3BucketEndpoint, -1, key, null, null);
				responseBuilder
					.location(uriS3)
					;
			}
			catch(URISyntaxException ex) {
				throw new WebApplicationException(ex);
			}
		}

		return responseBuilder.build();
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
		script.setDebug(modifyScript.debug);

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
		script.setDebug(modifyScript.debug);

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
				.withPrefix(getObjectKey(script.getId(), null));
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
		String prefix = String.format("scripts/%s", id);

		final String keyOrig = String.format("%s/%s", prefix, FILE_ORIGINAL);
		final String keyMini = String.format("%s/%s", prefix, FILE_MINIFIED);
		final String keyComp = String.format("%s/%s", prefix, FILE_COMPRESSED);
		final String keyCompMini = String.format("%s/%s", prefix, FILE_MINIFIED_COMPRESSED);

		// Do some crazy input stream branching
		final ConcurrentPipeStream pipeOrig = new ConcurrentPipeStream();
		final ConcurrentPipeStream pipeComp = new ConcurrentPipeStream();
		final ConcurrentPipeStream pipeMini = new ConcurrentPipeStream();
		final ConcurrentPipeStream pipeCompMini = new ConcurrentPipeStream();

		final GZIPOutputStream gzipOrig = new GZIPOutputStream(pipeComp.getOutputStream());
		final GZIPOutputStream gzipMini = new GZIPOutputStream(pipeCompMini.getOutputStream());

		final TeeOutputStream teeBranch = new TeeOutputStream(gzipOrig, pipeOrig.getOutputStream());
		final TeeInputStream teeOrig = new TeeInputStream(source, teeBranch, true);
		final TeeInputStream teeMini = new TeeInputStream(pipeMini.getInputStream(), gzipMini, true);

		final ObjectMetadata metaRaw = new ObjectMetadata();
		final ObjectMetadata metaComp = new ObjectMetadata();

		metaRaw.setContentType("text/javascript");
		metaComp.setContentType("text/javascript");
		metaComp.setContentEncoding("gzip");

		// Perform crazy async file upload
		Future<PutObjectResult> fOrig = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyOrig, teeOrig, metaRaw), CannedAccessControlList.PublicRead
			));
		Future<PutObjectResult> fComp = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyComp, pipeComp.getInputStream(), metaComp), CannedAccessControlList.PublicRead
			));
		Future<Void> fMinify = executorService.submit(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				Writer writerMini = new OutputStreamWriter(pipeMini.getOutputStream());
				Reader readerMini = new InputStreamReader(pipeOrig.getInputStream());
				JavaScriptCompressor compressor = new JavaScriptCompressor(readerMini, null);
				compressor.compress(writerMini, 16000, true, false, true, false);

				readerMini.close();
				writerMini.close();

				return null;
			}
		});
		Future<PutObjectResult> fMini = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyMini, teeMini, metaRaw), CannedAccessControlList.PublicRead
			));
		Future<PutObjectResult> fMiniComp = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyCompMini, pipeCompMini.getInputStream(), metaComp), CannedAccessControlList.PublicRead
			));
	}

	private String getObjectKey(String id, String file) {
		return String.format("%s/%s/%s", BASE_PATH, id, file == null ? StringUtils.EMPTY : file);
	}
}
