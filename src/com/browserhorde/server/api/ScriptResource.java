package com.browserhorde.server.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.entity.Script;
import com.google.inject.Inject;

@Path("scripts")
@Produces(MediaType.APPLICATION_JSON)
public class ScriptResource {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private EntityManager entityManager;
	@Inject private FileItemFactory fileFactory;

	@GET
	public Response listScripts() {
		return Response.ok().build();
	}

	@GET
	@Path("{id}")
	public Response getScript(@PathParam("id") String id) {
		return Response.ok().build();
	}

	@GET
	@Path("{id}.js")
	@Produces("application/javascript")
	public Response getScriptContent(@PathParam("id") String id) {
		id = StringUtils.trimToNull(id);
		if(id == null) {
			return Response.noContent().build();
		}

		Script script = entityManager.find(Script.class, id);

		// TODO: Get appropriate script instance
		// TODO: If we are using cloud front then we need to be able to change the domain
		try {
			// TODO: Don't forget bucket
			URI uriS3 = URIUtils.createURI("https", "s3.amazon.com", -1, "/scripts/" + id, null, null);
			return Response.status(302).location(uriS3).build();
		}
		catch(URISyntaxException ex) {
			log.error("Unable to create S3 URI", ex);
			return Response.serverError().build();
		}
	}

	@POST
	public Response createScript() {
		return Response.ok().build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadScript(
			@Context HttpServletRequest request,
			@FormParam("desc") @QueryParam("desc") String description
		) {
		ServletFileUpload upload = new ServletFileUpload(fileFactory);
		try {
			List<FileItem> items = upload.parseRequest(request);
			for(FileItem item : items) {
				if(item.isFormField()) {
					String name = item.getFieldName();
					String value = item.getString();
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
		return Response.ok().build();
	}

	@DELETE
	@Path("{id}")
	public Response deleteScript(@PathParam("id") String id) {
		return Response.ok().build();
	}
}
