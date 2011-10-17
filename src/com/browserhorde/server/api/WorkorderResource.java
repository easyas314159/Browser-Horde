package com.browserhorde.server.api;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.browserhorde.server.api.consumes.WorkorderCheckin;
import com.browserhorde.server.api.error.NoTasksException;
import com.browserhorde.server.api.produces.WorkorderCheckout;
import com.browserhorde.server.cache.Cache;
import com.browserhorde.server.cache.DistributedCache;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.gson.GsonTranscoder;
import com.browserhorde.server.gson.Visibility;
import com.browserhorde.server.gson.VisibilityLevel;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;

@Path("workorders")
@Produces({MediaType.APPLICATION_JSON})
public class WorkorderResource {
	private static final Integer DEFAULT_TIMEOUT = 3600;
	private static final String NS_ALL_WORKORDER = DigestUtils.md5Hex("workorders");

	private final Logger log = Logger.getLogger(getClass());

	@Inject private AmazonSQSAsync awsSQS;

	@Inject private Random rs;
	@Inject private EntityManager entityManager;

	private final Cache<String, WorkorderEntry> allWorkorders;

	@Inject
	public WorkorderResource(MemcachedClient memcached) {
		// FIXME: This should only be creating the cache once and sharing it across instances
		allWorkorders = new DistributedCache<WorkorderEntry>(
				memcached,
				new GsonTranscoder<WorkorderEntry>(WorkorderEntry.class),
				NS_ALL_WORKORDER
			);
	}

	@GET
	public Response checkout(
			@Context SecurityContext sec,
			@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId
		) {
		return checkoutForJob(sec, machineId, null);
	}

	@GET
	@Path("{id}")
	public Response checkoutForJob(
			@Context SecurityContext sec,
			@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId,
			@PathParam("id") String id
		) {
		Object entity = null;

		User user = (User)sec.getUserPrincipal();

		// TODO: Determine what kind of workorder we should be checking out depending on the user

		id = StringUtils.trimToNull(id);
		Job job = (id == null) ? randomJob() : entityManager.find(Job.class, id);
		Task task = randomTask(job);

		Date expires = null;
		if(task == null) {
			throw new NoTasksException();
		}
		else {
			String wo = UUID.randomUUID().toString();
			Integer timeout = ParamUtils.coalesce(task.getTimeout(), job.getTimeout(), DEFAULT_TIMEOUT);

			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, timeout);

			expires = c.getTime();

			entity = new WorkorderCheckout(wo, expires, task, null);
			WorkorderEntry entry = new WorkorderEntry(user == null ? null : user.getName(), machineId, task.getId());

			allWorkorders.put(wo, entry, expires);
		}

		entityManager.close();

		return Response
			.status(ApiStatus.OK)
			.entity(entity)
			.expires(expires)
			.build()
			;
	}

	@POST
	public Response checkin(
			@Context SecurityContext sec,
			@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId,
			WorkorderCheckin checkin
		) {

		WorkorderEntry entry = allWorkorders.get(checkin.getId());
		if(entry == null) {
			throw new NotFoundException();
		}
		else {
			Principal p = sec.getUserPrincipal();
			String userId = (p == null) ? null : p.getName();
			String taskId = checkin.getTask();

			if(StringUtils.equalsIgnoreCase(userId, entry.userId)
				&& StringUtils.equalsIgnoreCase(machineId, entry.machineId)
				&& StringUtils.equalsIgnoreCase(taskId, entry.taskId)
				) {

				Task task = entityManager.find(Task.class, taskId);
				if(task == null) {
					// TODO: Bail early
				}
				else {
					log.debug("Checkin success");

					log.debug(checkin.getData());
					// TODO: Pass off to data validation
					// All the parameters check out so this is probably the correct result
					// TODO: Push job details to statistics queue
				}
			}
			else {
				throw new NotFoundException();
			}
		}

		return Response.status(ApiStatus.ACCEPTED).build();
	}

	@DELETE
	@Path("{id}")
	public Response cancelWorkorder(
			@Context SecurityContext sec,
			@HeaderParam(ApiHeaders.X_HORDE_MACHINE_ID) String machineId,
			@PathParam("id") String id
		) {
		User user = (User)sec.getUserPrincipal();
		WorkorderEntry entry = allWorkorders.get(id);

		// TODO: More checking here to ensure people aren't terminating random WOs
		if(entry != null && user != null && user.getName().equals(entry.userId)) {
			allWorkorders.expire(id);
		}

		return Response.status(ApiStatus.NO_CONTENT).build();
	}

	private Job randomJob() {
		String r = UUID.randomUUID().toString();

		Query le = entityManager.createQuery(
				"select * from " + Job.class.getName()
				+ " where randomizer <= :randomizer ORDER BY randomizer DESC"
			);
		Query ge = entityManager.createQuery(
				"SELECT * FROM " + Job.class.getName()
				+ " WHERE randomizer >= :randomizer ORDER BY randomizer ASC"
			);
		le.setParameter("randomizer", r);
		ge.setParameter("randomizer", r);

		// FIXME: Horribly Inefficient without this
		//le.setMaxResults(1);
		//ge.setMaxResults(1);

		Job job = null;
		if(rs.nextBoolean()) {
			job = getFirstResult(le, ge);
		}
		else {
			job = getFirstResult(ge, le);
		}

		if(job != null) {
			job.setRandomizer(r);
			entityManager.merge(job);
		}

		return job;
	}
	
	private Task randomTask(Job job) {
		if(job == null) {
			return null;
		}

		String r = UUID.randomUUID().toString();

		Query le = entityManager.createQuery(
				"SELECT * FROM " + Task.class.getName()
				+ " WHERE job=:job AND randomizer <= :randomizer ORDER BY randomizer DESC"
			);
		Query ge = entityManager.createQuery(
				"SELECT * FROM " + Task.class.getName()
				+ " WHERE job=:job AND randomizer >= :randomizer ORDER BY randomizer ASC"
			);
		le.setParameter("job", job);
		ge.setParameter("job", job);

		le.setParameter("randomizer", r);
		ge.setParameter("randomizer", r);

		// FIXME: Horribly inefficient without this
		//le.setMaxResults(1);
		//ge.setMaxResults(1);

		Task task = null;
		if(rs.nextBoolean()) {
			task = getFirstResult(le, ge);
		}
		else {
			task = getFirstResult(ge, le);
		}

		if(task != null) {
			task.setRandomizer(r);
			entityManager.merge(task);
		}

		return task;
	}

	private <T> T getFirstResult(Query ... qs) {
		for(Query q : qs) {
			try {
				List<T> results = q.getResultList();
				if(results.size() < 1) {
					continue;
				}
				return results.get(0);
			}
			catch(NoResultException ex) {
			}
		}
		return null;
	}

	private static class WorkorderEntry {
		public String userId;
		public String machineId;
		public String taskId;

		public WorkorderEntry(String userId, String machineId, String taskId) {
			this.userId = userId;
			this.machineId = machineId;
			this.taskId = taskId;
		}
	}
}
