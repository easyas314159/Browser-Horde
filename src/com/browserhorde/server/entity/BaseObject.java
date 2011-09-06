package com.browserhorde.server.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;

import com.google.gson.annotations.Expose;
import com.spaceprogram.simplejpa.model.Ided;
import com.spaceprogram.simplejpa.model.TimestampEntityListener;
import com.spaceprogram.simplejpa.model.Timestamped;

@MappedSuperclass
@EntityListeners({TimestampEntityListener.class})
@XmlAccessorType(XmlAccessType.NONE)
public abstract class BaseObject implements Ided, Timestamped, Serializable {
	@Expose
	@XmlID
	@XmlAttribute
	private String id;

	@Expose
	@XmlElement
	private Date created;

	@Expose
	@XmlElement
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
}
