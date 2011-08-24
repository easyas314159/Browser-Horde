package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import com.google.gson.annotations.Expose;

@Entity
public class Script extends BaseObject {
	@Expose private User owner;

	@Expose private String name;
	@Expose private String docurl;
	@Expose private String description;

	@Expose private boolean debug;

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

	public String getDocurl() {
		return docurl;
	}
	public void setDocurl(String docurl) {
		this.docurl = docurl;
	}

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
