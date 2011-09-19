package com.browserhorde.server.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
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
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang.NotImplementedException;
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
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.consumes.ModifyScriptRequest;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.api.error.InvalidRequestException;
import com.browserhorde.server.aws.AmazonS3AsyncPutObject;
import com.browserhorde.server.entity.Script;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.browserhorde.server.util.ConcurrentPipeStream;
import com.google.inject.Inject;
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
	
	private final String S3_BUCKET;

	@Inject private EntityManager entityManager;
	@Inject private ExecutorService executorService;
	@Inject private FileItemFactory fileFactory;

	@Inject private AmazonS3 awsS3;

	@Inject
	public ScriptResource(ServletContext context) {
		S3_BUCKET = context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET);
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
		Object entity = null;
		
		User user = (User)sec.getUserPrincipal();

		if(entity == null) {
			id = StringUtils.trimToNull(id);
			if(id == null) {
				throw new InvalidRequestException();
			}
			else {
				Script script = entityManager.find(Script.class, id);
				if(script == null || !script.isOwnedBy(user)) {
					throw new ForbiddenException();
				}
				entity = script;
			}
		}

		return Response.ok(entity).build();
	}

	@GET
	@Path("{id}.js")
	@Produces({"text/javascript", "application/javascript", "application/x-javascript"})
	public Response getScriptContent(@Context HttpHeaders headers, @PathParam("id") String id) {
		id = StringUtils.trimToNull(id);
		if(id == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Script script = entityManager.find(Script.class, id);
		if(script == null) {
			return Response.status(Status.NOT_FOUND).build();
		}

		Map<String, Float> acceptedEncodings = new HashMap<String, Float>();
		List<String> acceptHeaders = headers.getRequestHeader(HttpHeaders.ACCEPT_ENCODING);
		if(acceptHeaders != null) {
			for(String acceptHeader : acceptHeaders) {
				acceptHeader = acceptHeader.trim();
				String encodings[] = acceptHeader.split("\\s*,\\s*");
				for(String encoding : encodings) {
					String details[] = encoding.split("\\s*(;|=)\\s*", 3);
					if(details.length == 1) {
						acceptedEncodings.put(details[0], 1.0f);
					}
					else if(details.length == 3 && details[1].equals("q") && details[2].matches("(0(\\.\\d{1,3})?)|(1(\\.0{1,3})?)")) {
						acceptedEncodings.put(details[0], Float.valueOf(details[2]));
					}
					else {
						log.debug(String.format("Bad Accept-Encoding header \'%s\'", encoding));
					}
				}
			}
		}

		boolean gzip = false;
		Float weightGzip = acceptedEncodings.get("gzip");
		Float weightIdent = acceptedEncodings.get("identity");
		Float weightAny = acceptedEncodings.get("*");

		if(weightIdent == null) {
			weightIdent = 0.001f;
		}

		if(weightGzip == null) {
			if(weightAny != null && weightIdent <= weightAny) {
				gzip = true;
			}
		}
		else {
			if(weightIdent <= weightGzip) {
				gzip = true;
			}
			else if(weightAny != null && weightIdent <= weightAny) {
				gzip = true;
			}
		}

		boolean mini = !script.isDebug();

		String res = gzip ? (mini ? FILE_MINIFIED_COMPRESSED : FILE_COMPRESSED) : (mini ? FILE_MINIFIED : FILE_ORIGINAL);
		String key = String.format(
				"%s/%s/%s",
				BASE_PATH,
				script.getId(),
				res
			);

		try {
			// TODO: If we are using cloud front then we need to be able to change the domain
			URI uriS3 = URIUtils.createURI("https", "s3.amazonaws.com", -1, S3_BUCKET + "/" + key, null, null);
			return Response.status(ApiStatus.TEMPORARY_REDIRECT).location(uriS3).build();
		}
		catch(URISyntaxException ex) {
			log.error("Unable to create S3 URI", ex);
			return Response.serverError().build();
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
		// TODO: Create a script

		throw new NotImplementedException();
	}

	@POST
	@Path("{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.REGISTERED)
	public Response updateScript(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			@PathParam("id") String scriptId,
			ModifyScriptRequest modifyScript
		) {
		User user = (User)sec.getUserPrincipal();

		// TODO: Modify an existing script
		throw new NotImplementedException();
	}

	/*
	@POST
	@Path("{id}")
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	@RolesAllowed(Roles.REGISTERED)
	public Response createScript(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			RequestContext request
		) {

		byte [] rawFile = null;
		Object entity = null;

		User user = (User)sec.getUserPrincipal();

		// TODO: Check user permissions
		Map<String, String> params = new HashMap<String, String>();
		if(entity == null) {
			ServletFileUpload upload = new ServletFileUpload(fileFactory);
			try {
				List<FileItem> scripts = new Vector<FileItem>();
				List<FileItem> items = upload.parseRequest(request);
				for(FileItem item : items) {
					if(item.isFormField()) {
						params.put(
								item.getFieldName(),
								item.getString()
							);
					}
					else {
						scripts.add(item);
					}
				}

				if(scripts.size() == 1) {
					// TODO: Overwrite params with form fields
					FileItem script = scripts.get(0);
					rawFile = script.get();
				}
				else {
					throw new InvalidRequestException();
				}
			}
			catch(FileUploadException ex) {
				log.warn("File upload exception", ex);
				throw new InvalidRequestException();
			}
		}

		URI location = null;
		String name = ParamUtils.asString(params.get("name"), "New Script");
		String desc = ParamUtils.asString(params.get("desc"));
		String docurl = ParamUtils.asString(params.get("docurl"));
		Boolean debug = ParamUtils.asBoolean("debug", Boolean.FALSE);

		if(entity == null) {
			Script script = new Script();

			script.setOwner(user);
			script.setName(name);
			script.setDescription(desc);
			script.setDocurl(docurl);

			script.setDebug(debug);

			entityManager.persist(script);

			location = ui.getBaseUriBuilder()
				.path(getClass())
				.path(script.getId())
				.build()
				;

			try {
				// TODO: Parse uploaded script and do a security check
				storeScript(
						rawFile,
						S3_BUCKET,
						script.getId()
					);
			}
			catch(IOException ex) {
				log.warn("Store failed!", ex);
			}

			entity = script;
		}
		else {
			throw new InvalidRequestException();
		}
		
		return Response
			.status(ApiStatus.CREATE)
			.location(location)
			.entity(entity)
			.build()
			;
	}
	*/

	@PUT
	@Path("{id}")
	@Consumes({"text/javascript", "application/javascript", "application/x-javascript"})
	public Response storeScript(@Context SecurityContext sec, @PathParam("id") String id, InputStream source) {
		User user = (User)sec.getUserPrincipal();

		Script script = entityManager.find(Script.class, id);
		if(script == null || !script.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			try {
				storeScript(source, S3_BUCKET, script.getId());
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
				.withBucketName(S3_BUCKET)
				.withPrefix(String.format("%s/%s", BASE_PATH, script.getId()));
			while(true) {
				ObjectListing listing = awsS3.listObjects(listObjects);

				for(S3ObjectSummary summary : listing.getObjectSummaries()) {
					awsS3.deleteObject(S3_BUCKET, summary.getKey());
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

	// TODO: This seems to be missing the ACL stuff
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

		metaRaw.setContentType("application/javascript");
		metaComp.setContentType("application/javascript");
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
}
