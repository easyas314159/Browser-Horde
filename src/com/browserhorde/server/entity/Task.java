package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.google.gson.annotations.Expose;

@Entity
public class Task extends BaseObject {
	@Expose
	private Job job;

	private String randomizer;

	// FIXME: Booleans are not persisting to the database
	@Expose
	private Boolean active;

	@Expose
	private Integer timeout;

	@ManyToOne
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}

	public String getRandomizer() {
		return randomizer;
	}
	public void setRandomizer(String randomizer) {
		this.randomizer = randomizer;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(Boolean active) {
		if(active == null) {
			active = Boolean.FALSE;
		}
		this.active = active;
	}

	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Override
	public boolean isOwnedBy(User user) {
		return getJob().isOwnedBy(user);
	}
}
