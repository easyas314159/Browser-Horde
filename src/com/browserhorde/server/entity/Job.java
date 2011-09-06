package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlElement;

import com.google.gson.annotations.Expose;

@Entity
public class Job extends BaseObject {
	@Expose
	@XmlElement
	private User owner;

	@Expose
	@XmlElement
	private String name;

	@Expose
	@XmlElement
	private String description;

	@Expose
	@XmlElement
	private String website;

	@Expose
	@XmlElement
	private String callback;

	@Expose
	@XmlElement
	private boolean ispublic;

	@Expose
	@XmlElement
	private boolean active;

	@Expose
	@XmlElement
	private Integer timeout;

	@Expose
	@XmlElement
	private Script script;

	private String randomizer;

	@ManyToOne
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getRandomizer() {
		return randomizer;
	}
	public void setRandomizer(String randomizer) {
		this.randomizer = randomizer;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getWebsite() {
		return website;
	}
	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
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

	@ManyToOne
	public Script getScript() {
		return script;
	}
	public void setScript(Script script) {
		this.script = script;
	}
}
