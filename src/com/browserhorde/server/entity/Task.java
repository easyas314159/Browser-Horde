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
	private boolean isactive;

	private Integer timeout;

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
	public boolean isIsactive() {
		return isactive;
	}
	public void setIsactive(boolean isactive) {
		this.isactive = isactive;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
}
