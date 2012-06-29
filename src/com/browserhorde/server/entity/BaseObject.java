package com.browserhorde.server.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;

@MappedSuperclass
@EntityListeners({TimestampEntityListener.class})
public abstract class BaseObject implements Ided, Timestamped, Serializable {
	@Expose
	@Id
	private String id;

	@Expose
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;

	@Expose
	@Temporal(TemporalType.TIMESTAMP)
	private Date updated;

	@Id
	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public Date getCreated() {
		return created;
	}
	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public Date getUpdated() {
		return updated;
	}
	@Override
	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	@Transient
	public boolean isOwnedBy(User user) {
		return false;
	}
}
