package com.browserhorde.server.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.zip.GZIPOutputStream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.GroupGrantee;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.Permission;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ApiResponseStatus;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.entity.Script;
import com.google.inject.Inject;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

@Path("scripts")
@Produces(MediaType.APPLICATION_JSON)
public class ScriptResource {
	private static final String BASE_PATH = "scripts";

	private static final String FILE_ORIGINAL = ".js";
	private static final String FILE_MINIFIED = "min.js";
	private static final String FILE_COMPRESSED = ".js.gz";
	private static final String FILE_MINIFIED_COMPRESSED = "min.js.gz";

	private final Logger log = Logger.getLogger(getClass());

	@Context private ServletContext context;

	@Inject private EntityManager entityManager;
	@Inject private ExecutorService executorService;
	@Inject private FileItemFactory fileFactory;

	@Inject private AmazonS3 awsS3;

	@GET
	public Response listScripts(
			@DefaultValue("") @QueryParam("q") String search
		) {
		ApiResponse response = null;

		// TODO: This needs to filter jobs by the user that owns them
		if(response == null) {
			Query query = entityManager.createQuery(
					"select * from " + Script.class.getName()
				);
			List<?> results = query.getResultList();
			response = new ResourceResponse(results);
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("{id}")
	public Response getScript(@PathParam("id") String id) {
		ApiResponse response = null;
		
		if(response == null) {
			id = StringUtils.trimToNull(id);
			if(id == null) {
				response = new InvalidRequestResponse();
			}
			else {
				Script script = entityManager.find(Script.class, id);
				if(script == null) {
					return Response.status(Status.NOT_FOUND).build();
				}
				response = new ResourceResponse(script);
			}
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("{id}.js")
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
		for(String acceptHeader : acceptHeaders) {
			acceptHeader = acceptHeader.trim();
			String encodings[] = acceptHeader.split("\\s*,\\s*");
			for(String encoding : encodings) {
				String details[] = encoding.split("\\s*(;|=)\\s*", 3);
				if(details.length == 1) {
					acceptedEncodings.put(details[0], null);
				}
				else if(details.length == 3 && details[1].equals("q") && details[2].matches("(0(\\.\\d{1,3})?)|(1(\\.0{1,3})?)")) {
					acceptedEncodings.put(details[0], Float.valueOf(details[2]));
				}
				else {
					log.debug(String.format("Bad Accept-Encoding header \'%s\'", encoding));
				}
			}
		}

		// TODO: Figure out if we should be gzipping the response

		boolean gzip = false;
		boolean mini = !script.isDebug();

		String bucket = context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET);
		String res = gzip ? (mini ? FILE_MINIFIED_COMPRESSED : FILE_COMPRESSED) : (mini ? FILE_MINIFIED : FILE_ORIGINAL);
		String key = String.format(
				"%s/%s/%s",
				BASE_PATH,
				script.getId(),
				res
			);

		try {
			// TODO: If we are using cloud front then we need to be able to change the domain
			URI uriS3 = URIUtils.createURI("https", "s3.amazonaws.com", -1, bucket + "/" + key, null, null);
			return Response.status(302).location(uriS3).build();
		}
		catch(URISyntaxException ex) {
			log.error("Unable to create S3 URI", ex);
			return Response.serverError().build();
		}
	}

	@POST
	@Consumes({
			MediaType.MULTIPART_FORM_DATA,
			MediaType.APPLICATION_FORM_URLENCODED
		})
	public Response createScript(
			@Context HttpServletRequest request,
			@FormParam("name") @QueryParam("name") String name,
			@FormParam("desc") @QueryParam("desc") String desc,
			@FormParam("docurl") @QueryParam("docurl") String docurl,
			@DefaultValue("true") @FormParam("debug") @QueryParam("debug") Boolean debug,
			@DefaultValue("false") @FormParam("shared") @QueryParam("shared") Boolean shared
		) {
		byte [] rawFile = null;
		ApiResponse response = null;

		// TODO: Check user permissions
		if(response == null) {
			RequestContext reqCtx = new ServletRequestContext(request);
			ServletFileUpload upload = new ServletFileUpload(fileFactory);
			if(ServletFileUpload.isMultipartContent(reqCtx)) {
				try {
					Map<String, String> params = new HashMap<String, String>();
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
						response = new InvalidRequestResponse();
					}
				}
				catch(FileUploadException ex) {
					response = new InvalidRequestResponse();
					log.info("", ex);
				}
			}
			else {
				// TODO: Handle non-multipart upload
			}
		}

		if(response == null) {

			Script script = new Script();

			// TODO: Set script owner
			script.setName(name);
			script.setDescription(desc);
			script.setDocurl(docurl);

			script.setDebug(debug);
			script.setShared(shared);

			entityManager.persist(script);

			try {
				// TODO: Parse uploaded script and do a security check
				storeScript(
						rawFile,
						context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET),
						script.getId()
					);
			}
			catch(IOException ex) {
				log.warn("Store failed!", ex);
			}

			response = new ResourceResponse(script);
		}
		else {
			response = new InvalidRequestResponse();
		}

		return Response.ok(response).build();
	}

	@PUT
	@Path("{id}")
	public Response updateScript(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	public Response deleteScript(@PathParam("id") String id) {
		ApiResponse response = null;

		// TODO: If the user isn't logged in then request denied
		// TODO: Be careful when deleting scripts especially public ones
		if(response == null) {
			Script script = entityManager.find(Script.class, id);
			if(script == null) {
				response = new InvalidRequestResponse();
			}
			else {
				entityManager.remove(script);
				response = new ApiResponse(ApiResponseStatus.OK);
			}
		}

		return Response.ok(response).build();
	}

	private void storeScript(byte [] data, String bucket, String id) throws IOException {
		String prefix = String.format("scripts/%s", id);

		AccessControlList acl =  awsS3.getBucketAcl(bucket);
		acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);

		final String keyOrig = String.format("%s/%s", prefix, FILE_ORIGINAL);
		final String keyMini = String.format("%s/%s", prefix, FILE_MINIFIED);
		final String keyComp = String.format("%s/%s", prefix, FILE_COMPRESSED);
		final String keyCompMini = String.format("%s/%s", prefix, FILE_MINIFIED_COMPRESSED);

		final ObjectMetadata metaRaw = new ObjectMetadata();
		final ObjectMetadata metaComp = new ObjectMetadata();

		Date lastModified = new Date();
		String contentType = "application/javascript";

		metaRaw.setContentType(contentType);
		metaRaw.setLastModified(lastModified);

		metaComp.setContentType(contentType);
		metaComp.setContentEncoding("gzip");
		metaComp.setLastModified(lastModified);

		byte [] tempBuffer;
		InputStream tempInput;

		uploadData(data, bucket, keyOrig, metaRaw, acl);

		final ByteArrayOutputStream dupGzipOrig = new ByteArrayOutputStream();
		final GZIPOutputStream gzipOrig = new GZIPOutputStream(dupGzipOrig);

		gzipOrig.write(data);
		gzipOrig.close();

		tempBuffer = dupGzipOrig.toByteArray();
		uploadData(tempBuffer, bucket, keyComp, metaComp, acl);

		tempInput = new ByteArrayInputStream(data);
		final ByteArrayOutputStream dupMini = new ByteArrayOutputStream();

		Reader tempReader = new InputStreamReader(tempInput);
		Writer tempWriter = new OutputStreamWriter(dupMini);

		JavaScriptCompressor compressor = new JavaScriptCompressor(tempReader, null);
		compressor.compress(tempWriter, 16000, true, false, true, false);

		tempReader.close();
		tempWriter.close();

		tempBuffer = dupMini.toByteArray();
		uploadData(tempBuffer, bucket, keyMini, metaRaw, acl);

		final ByteArrayOutputStream dupGzipMini = new ByteArrayOutputStream();
		final GZIPOutputStream gzipMini = new GZIPOutputStream(dupGzipMini);

		gzipMini.write(tempBuffer);
		gzipMini.close();

		uploadData(dupGzipMini.toByteArray(), bucket, keyCompMini, metaComp, acl);
	}

	private PutObjectResult uploadData(byte [] buffer, String bucket, String key, ObjectMetadata meta, AccessControlList acl) throws IOException {
		meta.setContentLength(buffer.length);
		ByteArrayInputStream input = new ByteArrayInputStream(buffer);
		PutObjectResult result = awsS3.putObject(bucket, key, input, meta);
		awsS3.setObjectAcl(bucket, key, acl);
		input.close();
		return result;
	}
	
	/*
	private void storeScript(InputStream source, String bucket, String id) throws IOException {
		String prefix = String.format("scripts/%s", id);

		final String keyOrig = String.format("%s/%s", prefix, FILE_ORIGINAL);
		final String keyMini = String.format("%s/%s", prefix, FILE_MINIFIED);
		final String keyComp = String.format("%s/%s", prefix, FILE_COMPRESSED);
		final String keyCompMini = String.format("%s/%s", prefix, FILE_MINIFIED_COMPRESSED);

		// Do some crazy input stream branching
		final PipeStream pipeOrig = new PipeStream();
		final PipeStream pipeComp = new PipeStream();
		final PipeStream pipeMini = new PipeStream();
		final PipeStream pipeCompMini = new PipeStream();

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
		// FIXME: This locks up if there are less than 3 executors in the thread pool
		Future<PutObjectResult> fOrig = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyOrig, teeOrig, metaRaw)
			));
		Future<PutObjectResult> fComp = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyComp, pipeComp.getInputStream(), metaComp)
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
				awsS3, new PutObjectRequest(bucket, keyMini, teeMini, metaRaw)
			));
		Future<PutObjectResult> fMiniComp = executorService.submit(new AmazonS3AsyncPutObject(
				awsS3, new PutObjectRequest(bucket, keyCompMini, pipeCompMini.getInputStream(), metaComp)
			));
	}
	 */
}
