package com.browserhorde.server.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.http.client.utils.URIUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.browserhorde.server.Configurator;
import com.browserhorde.server.aws.BucketManager;
import com.browserhorde.server.aws.DomainManager;

public class ScriptServlet extends HttpServlet {
	private Logger log = Logger.getLogger(getClass());

	private static final String PARAM_JOB_ID = "job";

	private DiskFileItemFactory fileItemFactory = null;
	private FileCleaningTracker fileCleaningTracker = null;
	
	private String awsS3Bucket = null;

	private AmazonS3 awsS3 = null;
	private AmazonSimpleDBAsync awsSimpleDB = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ServletContext context = config.getServletContext();

		fileItemFactory = new DiskFileItemFactory();
		fileCleaningTracker = new FileCleaningTracker();
		fileItemFactory.setFileCleaningTracker(fileCleaningTracker);

		awsSimpleDB = (AmazonSimpleDBAsync)context.getAttribute(Configurator.AWS_SIMPLEDB);

		awsS3 = (AmazonS3)context.getAttribute(Configurator.AWS_S3);
	}
	
	@Override
	public void destroy() {
		super.destroy();

		fileCleaningTracker.exitWhenFinished();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		String path = req.getServletPath();
		if(path.equals("/script")) {
			String jobId = req.getParameter(PARAM_JOB_ID);
			if(jobId == null) {
				rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else {
				// TODO: Check job privacy
				GetAttributesRequest attrRequest = new GetAttributesRequest(DomainManager.getJobs(), jobId);
				GetAttributesResult attrResult = awsSimpleDB.getAttributes(attrRequest);

				Map<String, String> job = getAttributeMap(attrResult.getAttributes());

				String scriptId = job.get("script");
				if(scriptId == null) {
					rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
				else {
					try {
						// TODO: Look into how to do this with deflate
						URI uri = URIUtils.createURI(
								"https",
								"s3.amazonaws.com",
								-1,
								BucketManager.getObjectPath(BucketManager.getBucket("scripts"), scriptId),
								null,
								null
							);
						rsp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
						rsp.setHeader("Location", uri.toString());
					}
					catch(URISyntaxException ex) {
						rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						log.warn("", ex);
					}
				}
			}
		}
		else {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		String path = req.getServletPath();
		if(path.equals("/script")) {
			RequestContext context = new ServletRequestContext(req);
			FileUpload upload = new ServletFileUpload(fileItemFactory);

			try {
				List<FileItem> items = upload.parseRequest(context);
			}
			catch(FileUploadException ex) {
				rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				log.warn("File upload failed", ex);
			}
		}
		else {
			rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	protected Map<String, String> getAttributeMap(Collection<Attribute> attrs) {
		Map<String, String> map = new HashMap<String, String>();
		for(Attribute attr : attrs) {
			map.put(attr.getName(), attr.getValue());
		}
		return map;
	}
}
