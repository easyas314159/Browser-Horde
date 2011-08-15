package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.spaceprogram.simplejpa.model.IdedTimestampedBase;

@Entity
public class Task extends IdedTimestampedBase {
	@ManyToOne(targetEntity=Job.class)
	public Job job;

	public String randomizer;

	private boolean ispublic;
	private boolean active;

	private Integer timeout;

	// TODO: Were do we keep the data?

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

	public boolean isIspublic() {
		return ispublic;
	}
	public void setIspublic(boolean ispublic) {
		this.ispublic = ispublic;
	}

	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}

	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
}
