package com.browserhorde.server.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

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
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.ResourceResponse;
import com.browserhorde.server.entity.Script;
import com.google.inject.Inject;

@Path("scripts")
@Produces(MediaType.APPLICATION_JSON)
public class ScriptResource {
	private final Logger log = Logger.getLogger(getClass());

	@Context private ServletContext context;

	@Inject private EntityManager entityManager;
	@Inject private ExecutorService executorService;
	@Inject private FileItemFactory fileFactory;

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
	@Produces("application/javascript")
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
		String res = null;

		// TODO: This logic ignores the presence of a minified version
		if(gzip) {
			res = script.getCompressed();
		}
		if(res == null) {
			res = script.getOriginal();
			if(res == null) {
				// TODO: Should this return something other than 404?
				return Response.status(Status.NOT_FOUND).build();
			}
		}

		// TODO: If we are using cloud front then we need to be able to change the domain
		try {
			String path = String.format("/%s/%s", context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET), res);
			URI uriS3 = URIUtils.createURI("https", "s3.amazon.com", -1, path, null, null);
			return Response.status(302).location(uriS3).build();
		}
		catch(URISyntaxException ex) {
			log.error("Unable to create S3 URI", ex);
			return Response.serverError().build();
		}
	}

	@POST
	public Response createScript() {
		throw new NotImplementedException();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadScript(
			@Context HttpServletRequest request,
			@FormParam("name") @QueryParam("name") String name,
			@FormParam("desc") @QueryParam("desc") String desc,
			@FormParam("docurl") @QueryParam("docurl") String docurl,
			@FormParam("debug") @QueryParam("debug") String debug,
			@FormParam("shared") @QueryParam("shared") String shared
		) {
		// TODO: Check user permissions

		ServletFileUpload upload = new ServletFileUpload(fileFactory);
		try {
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item : items) {
				if(item.isFormField()) {
					String n = item.getFieldName();
					String v = item.getString();
				}
				else {
					
				}
			}
		} catch(FileUploadException ex) {
			log.info("Script upload failed!", ex);
		}
		return Response.ok().build();
	}

	@PUT
	@Path("{id}")
	public Response updateScript(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	public Response deleteScript(@PathParam("id") String id) {
		throw new NotImplementedException();
	}
}
