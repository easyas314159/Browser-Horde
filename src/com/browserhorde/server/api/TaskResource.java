package com.browserhorde.server.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.http.client.utils.URIUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.api.consumes.ModifyTaskRequest;
import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.inject.QueueGZIP;
import com.browserhorde.server.queue.ProcessObject;
import com.browserhorde.server.security.Roles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;

//@Path("tasks")
@Path("jobs/{job_id}/tasks") // TODO: This might be more appropriate since tasks always belong to a job
@Produces({MediaType.APPLICATION_JSON})
public class TaskResource {
	@Inject private GsonBuilder gsonBuilder;

	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET) private String awsS3Bucket;
	@Inject @Named(ServletInitOptions.AWS_S3_BUCKET_ENDPOINT) private String awsS3BucketEndpoint;

	@Inject private AmazonS3 awsS3;

	@Inject private AmazonSQSAsync awsSQS;

	@Inject @QueueGZIP private String awsQueueGZIP;

	@Inject private EntityManager entityManager;

	@GET
	@RolesAllowed(Roles.REGISTERED)
	public Response listTasks(@Context SecurityContext sec, @PathParam("job_id") String id) {
		Object response = null;
		User user = (User)sec.getUserPrincipal();

		Job job = entityManager.find(Job.class, id);
		if(job == null || !job.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			Query query = entityManager.createQuery(
					"select * from " + Task.class.getName()
					+ " where job=:job"
				);
			query.setParameter("job", job);
			response = query.getResultList();
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("{task_id}")
	public Response getTask(
			@Context SecurityContext sec,
			@PathParam("job_id") String jobId,
			@PathParam("task_id") String taskId
		) {
		Task task = entityManager.find(Task.class, taskId);
		if(task == null) {
			throw new NotFoundException();
		}

		return Response
			.status(ApiStatus.OK)
			.entity(task)
			.build()
			;
	}

	@GET
	@Path("{task_id}/data")
	public Response getTaskData(
			@HeaderParam(HttpHeaders.IF_MODIFIED_SINCE) Date modifiedSince,
			@HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
			@PathParam("job_id") String jobId,
			@PathParam("task_id") String taskId
		) {

		Task task = entityManager.find(Task.class, taskId);
		if(task == null) {
			throw new NotFoundException();
		}

		String key = task.getAttachmentKey();

		// TODO: This needs switch between compressed and uncompresseed versions

		if(userAgent.indexOf("AppleWebKit") < 0) {
			try {
				URI uriS3 = URIUtils.createURI("http", awsS3BucketEndpoint, -1, key, null, null);
				return Response
					.status(ApiStatus.FOUND)
					.location(uriS3)
					.build()
					;
			} catch(URISyntaxException ex) {
				throw new WebApplicationException(ex);
			}
		}
		// HACK: This works around webkit bug 57600
		// https://bugs.webkit.org/show_bug.cgi?id=57600
		else {
			S3Object object = awsS3.getObject(awsS3Bucket, key);
			ObjectMetadata metadata = object.getObjectMetadata();

			Date lastModified = metadata.getLastModified();
			if(modifiedSince != null && lastModified != null && lastModified.after(modifiedSince)) {
				return Response
					.status(ApiStatus.NOT_MODIFIED)
					.build();
			}

			ResponseBuilder rb = Response
				.status(ApiStatus.OK)
				;
			for(Map.Entry<String, Object> entry : metadata.getRawMetadata().entrySet()) {
				rb.header(entry.getKey(), entry.getValue());
			}

			return rb
				.entity(object.getObjectContent())
				.build();
		}
	}

	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createTask(
			@Context SecurityContext sec,
			@Context UriInfo ui,
			@PathParam("job_id") String jobId,
			ModifyTaskRequest taskCreate
		) {

		User user = (User)sec.getUserPrincipal();
		Job job = entityManager.find(Job.class, jobId);

		if(job == null || !job.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		Task task = new Task();
		task.setRandomizer(UUID.randomUUID().toString());
		task.setJob(job);
		task.setActive(taskCreate.active);
		if(taskCreate.timeout != null && taskCreate.timeout > 0) {
			task.setTimeout(taskCreate.timeout);
		}

		entityManager.persist(task);
		URI location = ui.getAbsolutePathBuilder()
			.path(task.getId())
			.build()
			;

		return Response
			.status(ApiStatus.CREATE)
			.location(location)
			.entity(task)
			.build()
			;
	}

	@POST
	@Path("{task_id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateTask(
			@Context SecurityContext sec,
			@PathParam("job_id") String jobId,
			@PathParam("task_id") String taskId,
			ModifyTaskRequest modifyTask
		) {

		User user = (User)sec.getUserPrincipal();
		Task task = entityManager.find(Task.class, taskId);
		if(task == null || !task.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		if(!task.getJob().getId().equalsIgnoreCase(jobId)) {
			Job job = entityManager.find(Job.class, jobId);
			if(job != null && job.isOwnedBy(user)) {
				task.setJob(job);
			}
		}
		
		task.setActive(modifyTask.active);
		task.setTimeout(modifyTask.timeout);

		entityManager.merge(task);

		return Response
			.status(ApiStatus.OK)
			.entity(task)
			.build()
			;
	}

	@PUT
	@Path("{task_id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@RolesAllowed(Roles.REGISTERED)
	public Response storeTaskData(
			@Context SecurityContext sec,
			@PathParam("job_id") String jobId,
			@PathParam("task_id") String taskId,
			JsonElement source
		) {
		User user = (User)sec.getUserPrincipal();

		Task task = entityManager.find(Task.class, taskId);
		if(task == null || !task.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		String key = task.getAttachmentKey();

		// FIXME: This needs to catch malformed JSON exceptions
		byte [] raw = gsonBuilder.create().toJson(source).getBytes(Charset.forName("UTF-8"));
		InputStream stream = new ByteArrayInputStream(raw);

		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setLastModified(new Date());
		metadata.setContentType("application/json; charset=utf-8");

		PutObjectRequest putRequest = new PutObjectRequest(awsS3Bucket, key, stream, metadata);

		awsS3.putObject(putRequest);
		awsS3.setObjectAcl(awsS3Bucket, key, CannedAccessControlList.PublicRead);

		Gson gson = gsonBuilder.create();

		ProcessObject process = new ProcessObject(awsS3Bucket, key);
		awsSQS.sendMessage(new SendMessageRequest(awsQueueGZIP, gson.toJson(process)));

		// TODO: Increment billing

		return Response
			.status(ApiStatus.ACCEPTED)
			.build()
			;
	}

	@DELETE
	@Path("{task_id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteTask(
			@Context SecurityContext sec,
			@PathParam("job_id") String jobId,
			@PathParam("task_id") String taskId
		) {
		User user = (User)sec.getUserPrincipal();

		Task task = entityManager.find(Task.class, taskId);
		if(task == null || !task.isOwnedBy(user)) {
			throw new ForbiddenException();
		}

		entityManager.remove(task);

		// TODO: Delete task data
		// TODO: Delete any associated results

		return Response.status(ApiStatus.NO_CONTENT).build();
	}
}
