package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.annotations.Expose;

@Entity
public class Task extends BaseObject {
	@Expose
	@XmlElement
	public Job job;

	public String randomizer;

	@Expose
	@XmlElement
	private boolean ispublic;

	@Expose
	@XmlElement
	private boolean active;

	@Expose
	@XmlElement
	private Integer timeout;

	// TODO: Were do we keep the data?

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
