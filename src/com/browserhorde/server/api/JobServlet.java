package com.browserhorde.server.api;

import java.io.IOException;
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
import javax.servlet.http.HttpSession;

import net.spy.memcached.MemcachedClient;

import org.apache.log4j.Logger;

import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.browserhorde.server.Configurator;
import com.browserhorde.server.SessionAttributes;
import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.InvalidRequestResponse;
import com.browserhorde.server.api.json.NoTasksResponse;
import com.browserhorde.server.api.json.RequestDeniedResponse;
import com.browserhorde.server.api.json.WorkOrderResponse;
import com.browserhorde.server.aws.DomainManager;
import com.browserhorde.server.cache.Cache;
import com.browserhorde.server.cache.DistributedCache;
import com.browserhorde.server.cache.SimpleCache;
import com.browserhorde.server.util.GsonUtils;
import com.browserhorde.server.util.Identifier;
import com.browserhorde.server.util.ParamUtils;
import com.google.gson.Gson;

public final class JobServlet extends HttpServlet {
	private final Logger log = Logger.getLogger(getClass());

	private static final String INIT_PARAM_TASK_DEFAULT_TIMEOUT = "task.default_timeout";

	private static final String NS_CHECKED_OUT_JOBS = "ns_CheckOutJobs";

	private static final String PARAM_JOB_ID = "job";

	private Long defaultTaskTimeout = null;

	private AmazonSimpleDBAsync awsSimpleDB = null;

	private Cache<String, WorkOrderResponse> checkedOutJobs = null;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		ServletContext context = config.getServletContext();

		MemcachedClient memcached = (MemcachedClient)context.getAttribute(Configurator.MEMCAHED);
		if(memcached == null) {
			checkedOutJobs = new SimpleCache<String, WorkOrderResponse>();
		}
		else {
			checkedOutJobs = new DistributedCache<WorkOrderResponse>(memcached, null, NS_CHECKED_OUT_JOBS);
		}

		awsSimpleDB = (AmazonSimpleDBAsync)context.getAttribute(Configurator.AWS_SIMPLEDB);

		defaultTaskTimeout = ParamUtils.asLong(config.getInitParameter(INIT_PARAM_TASK_DEFAULT_TIMEOUT), 600000L);
		log.info(String.format("Default task timeout: %d", defaultTaskTimeout));
	}
	@Override
	public void destroy() {
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		ApiResponse response = null;
		String path = req.getServletPath();
		if(path.equals("/job/checkout")) {
			response = checkoutTask(req, rsp);
		}
		else {
			response = new InvalidRequestResponse();
		}

		if(response != null) {
			Gson gson = GsonUtils.newGson();
			gson.toJson(response, rsp.getWriter());
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse rsp) throws ServletException, IOException {
		String path = req.getServletPath();
		log.debug(String.format("POST %s", path));
	}

	private ApiResponse checkoutTask(HttpServletRequest req, HttpServletResponse rsp) {
		HttpSession session = req.getSession();
		if(session == null) {
			return new RequestDeniedResponse();
		}

		// TODO: Check if this is a logged in user

		String jobId = (String)session.getAttribute(SessionAttributes.JOB_ID);
		Long jobExpires = (Long)session.getAttribute(SessionAttributes.JOB_EXPIRES);

		if(jobId != null && jobExpires != null && System.currentTimeMillis() < jobExpires) {
			return new RequestDeniedResponse("This session already has a job checked out");
		}

		List<Attribute> jobAttr = null;
		GetAttributesRequest reqAttr = null;
		jobId = ParamUtils.asString(req.getParameter(PARAM_JOB_ID));
		if(jobId != null) {
			// TODO: Check job privacy
			reqAttr = new GetAttributesRequest(DomainManager.getJobs(), jobId); // TODO: Add domain name
			GetAttributesResult result = awsSimpleDB.getAttributes(reqAttr);
			jobAttr = result.getAttributes();
		}
		else {
			// TODO: Randomize selection
			// TODO: Check job privacy
			SelectRequest selectRequest = new SelectRequest(
					String.format("select * from `%s` where `active`='1' limit 1", DomainManager.getJobs()),
					false
				);
			SelectResult selectResult = awsSimpleDB.select(selectRequest);
			List<Item> items = selectResult.getItems();
			if(items.size() == 1) {
				Item item = items.get(0);
				jobId = item.getName();
				jobAttr = item.getAttributes();
			}
		}
		if(jobAttr == null || jobAttr.size() == 0) {
			return new NoTasksResponse();
		}

		String taskId = null;
		List<Attribute> taskAttr = null;
		{
			// TODO: Randomize selection
			// TODO: Check task privacy
			SelectRequest selectRequest = new SelectRequest(
					String.format("select * from `%s` where `active`='1' limit 1", DomainManager.getTasks()),
					false
				);
			SelectResult selectResult = awsSimpleDB.select(selectRequest);
			List<Item> items = selectResult.getItems();
			if(items.size() == 1) {
				Item item = items.get(0);
				taskId = item.getName();
				taskAttr = item.getAttributes();
			}
		}
		if(taskAttr == null || taskAttr.size() == 0) {
			return new NoTasksResponse();
		}

		// TODO: Generate Work Order ID
		Map<String, String> job = getAttributeMap(jobAttr);
		Map<String, String> task = getAttributeMap(taskAttr);

		String woId = Identifier.generate(16);
		WorkOrderResponse wo = new WorkOrderResponse(woId);

		// TODO: Finish building work order
		/* PSUEDO-CODE
		Generate work order
		Set task timeout
		Retrieve task data

		Store workorder request in cache with given timeout
		*/
		return wo;
	}

	protected Map<String, String> getAttributeMap(Collection<Attribute> attrs) {
		Map<String, String> map = new HashMap<String, String>();
		for(Attribute attr : attrs) {
			map.put(attr.getName(), attr.getValue());
		}
		return map;
	}
}
