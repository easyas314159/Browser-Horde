package com.browserhorde.server.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.google.gson.annotations.Expose;
import com.spaceprogram.simplejpa.model.Ided;
import com.spaceprogram.simplejpa.model.TimestampEntityListener;
import com.spaceprogram.simplejpa.model.Timestamped;

@MappedSuperclass
@EntityListeners({TimestampEntityListener.class})
public abstract class BaseObject implements Ided, Timestamped, Serializable {
	@Expose
	private String id;

	@Expose
	private Date created;

	@Expose
	private Date updated;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
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
