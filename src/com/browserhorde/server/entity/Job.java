package com.browserhorde.server.entity;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;

@Entity
public class Job extends BaseObject {
	@Expose
	private User owner;

	@Expose
	private String name;

	@Expose
	private String description;

	@Expose
	private String website;

	@Expose
	private String callback;

	@Expose
	private boolean active;

	@Expose
	private Integer timeout;

	@Expose
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
	public void setWebsite(URL website) {
		if(website == null) {
			setWebsite((String)null);
		}
		else {
			setWebsite(website.toString());
		}
	}
	public void setWebsite(String website) {
		this.website = website;
	}

	public String getCallback() {
		return callback;
	}
	public void setCallback(URL callback) {
		if(callback == null) {
			setCallback((String)null);
		}
		else {
			setCallback(callback.toString());
		}
	}
	public void setCallback(String callback) {
		this.callback = callback;
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
	
	@Override
	public boolean isOwnedBy(User user) {
		return StringUtils.equals(user.getId(), owner.getId());
	}
}
