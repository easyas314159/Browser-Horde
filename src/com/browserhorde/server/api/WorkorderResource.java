package com.browserhorde.server.api;

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
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import net.spy.memcached.MemcachedClient;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.api.json.ApiResponse;
import com.browserhorde.server.api.json.ApiResponseStatus;
import com.browserhorde.server.api.json.NoTasksResponse;
import com.browserhorde.server.api.json.WorkorderResponse;
import com.browserhorde.server.cache.Cache;
import com.browserhorde.server.cache.DistributedCache;
import com.browserhorde.server.cache.GsonTranscoder;
import com.browserhorde.server.entity.Job;
import com.browserhorde.server.entity.Task;
import com.browserhorde.server.entity.User;
import com.browserhorde.server.util.ParamUtils;
import com.browserhorde.server.util.Randomizer;
import com.google.inject.Inject;

@Path("workorders")
@Produces(MediaType.APPLICATION_JSON)
public class WorkorderResource {
	@Inject private Randomizer randomizer;

	@Inject private Random rs;
	@Inject private EntityManager entityManager;

	private static final Integer DEFAULT_TIMEOUT = 3600;
	private static final String NS_ALL_WORKORDER = DigestUtils.md5Hex("workorders");

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
	public Response checkoutWorkorder(@Context SecurityContext sec, @Context HttpHeaders headers) {
		return checkoutWorkorderForJob(sec, headers, null);
	}

	@GET
	@Path("{id}")
	public Response checkoutWorkorderForJob(@Context SecurityContext sec, @Context HttpHeaders headers, @PathParam("id") String id) {
		// Play some tricks with the modified header to try and track the user

		ApiResponse response = null;

		User user = (User)sec.getUserPrincipal();

		// TODO: Determine what kind of workorder we should be checking out depending on the user

		id = StringUtils.trimToNull(id);
		Job job = (id == null) ? randomJob() : entityManager.find(Job.class, id);
		Task task = randomTask(job);

		if(task == null) {
			response = new NoTasksResponse();
		}
		else {
			String wo = UUID.randomUUID().toString();
			Integer timeout = ParamUtils.coalesce(task.getTimeout(), job.getTimeout(), DEFAULT_TIMEOUT);

			Calendar c = Calendar.getInstance();
			c.add(Calendar.SECOND, timeout);
			
			Date expires = c.getTime();

			response = new WorkorderResponse(wo, expires, task, null);
			WorkorderEntry entry = new WorkorderEntry(user == null ? null : user.getName(), task.getId());

			allWorkorders.put(wo, entry, expires);
		}

		entityManager.close();

		return Response
			.ok(response)
			.build()
			;
	}

	@POST
	@Path("{id}")
	public Response checkinWorkorder(
			@Context SecurityContext sec,
			@PathParam("id") String id
		) {

		WorkorderEntry entry = allWorkorders.get(id);
		if(entry == null) {
			// TODO: return not found or something
		}
		else {
			
		}

		throw new NotImplementedException();
	}

	@DELETE
	@Path("{id}")
	public Response cancelWorkorder(@Context SecurityContext sec, @PathParam("id") String id) {
		User user = (User)sec.getUserPrincipal();
		WorkorderEntry entry = allWorkorders.get(id);

		if(entry != null && user != null && user.getName().equals(entry.user)) {
			allWorkorders.expire(id);
		}

		return Response.ok(new ApiResponse(ApiResponseStatus.OK)).build();
	}

	private Job randomJob() {
		String r = randomizer.nextRandomizer();

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

		String r = randomizer.nextRandomizer();

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
		public String user;
		public String task;

		public WorkorderEntry(String user, String task) {
			this.user = user;
			this.task = task;
		}
	}
}
