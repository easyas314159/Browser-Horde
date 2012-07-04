package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;

@Entity
public class Script extends BaseObject {
	@Expose private User owner;

	@Expose private String name;
	@Expose private String description;

	@Expose private String headRevision;

	// TODO: Scripts should tie into required libraries

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

	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getHeadRevision() {
		return headRevision;
	}
	public void setHeadRevision(String headRevision) {
		this.headRevision = headRevision;
	}

	@Override
	public boolean isOwnedBy(User user) {
		return StringUtils.equals(user.getId(), getOwner().getId());
	}
}
