package com.browserhorde.server.api;

import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManager;
import javax.persistence.Query;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.api.error.ForbiddenException;
import com.browserhorde.server.api.error.InvalidRequestException;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.security.Roles;
import com.google.inject.Inject;

@Path("tasks")
@Produces({MediaType.APPLICATION_JSON})
public class TaskResource {
	@Inject EntityManager entityManager;

	@GET
	@Path("{job}")
	@RolesAllowed(Roles.REGISTERED)
	public Response listTasks(@PathParam("job") String jobId) {
		Job job = null;
		Object response = null;

		jobId = StringUtils.trimToNull(jobId);
		if(jobId == null) {
			throw new InvalidRequestException();
		}
		else {
			job = entityManager.find(Job.class, jobId);
			if(job == null) {
				throw new InvalidRequestException();
			}
		}

		if(response == null) {
			Query query = entityManager.createQuery(
					"select * from " + Task.class.getName()
					+ " where job=:job"
				);
			query.setParameter("job", job);
			List<?> results = query.getResultList();
			response = results;
		}

		return Response.ok(response).build();
	}

	@GET
	@Path("{job}/{task}")
	public Response getTask(
			@PathParam("job") String jobId,
			@PathParam("task") String taskId
		) {
		Object response = null;

		jobId = StringUtils.trimToNull(jobId);
		taskId = StringUtils.trimToNull(taskId);
		if(jobId == null || taskId == null) {
			throw new InvalidRequestException();
		}

		if(response == null) {
			Task task = entityManager.find(Task.class, taskId);
			if(task == null || !task.getJob().getId().equals(jobId)) {
				return Response.status(Status.NOT_FOUND).build();
			}
			response = task;
		}

		return Response.ok(response).build();
	}

	@POST
	@RolesAllowed(Roles.REGISTERED)
	public Response createTask(
			@FormParam("job") String jobId,
			@FormParam("timeout") Integer timeout,
			@FormParam("active") @DefaultValue("true") Boolean active,
			@FormParam("public") @DefaultValue("true") Boolean freeforall
		) {

		Object response = null;

		Job job = entityManager.find(Job.class, jobId);
		if(job == null) {
			throw new InvalidRequestException();
		}
		else {
			Task task = new Task();

			task.setRandomizer(UUID.randomUUID().toString());
			task.setJob(job);
			task.setActive(active);
			if(timeout > 0) {
				task.setTimeout(timeout);
			}

			entityManager.persist(task);
		}

		return Response.ok(response).build();
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@RolesAllowed(Roles.REGISTERED)
	public Response uploadTask() {
		throw new NotImplementedException();
	}

	@PUT
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response updateTask(@PathParam("id") String id) {
		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	@RolesAllowed(Roles.REGISTERED)
	public Response deleteTask(@Context SecurityContext sec, @PathParam("id") String id) {
		User user = (User)sec.getUserPrincipal();

		Task task = entityManager.find(Task.class, id);
		if(task == null || !task.isOwnedBy(user)) {
			throw new ForbiddenException();
		}
		else {
			entityManager.remove(task);
			// TODO: Delete any associated results
		}

		return Response.status(ApiStatus.NO_CONTENT).build();
	}
}
