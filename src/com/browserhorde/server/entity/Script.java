package com.browserhorde.server.entity;

import javax.persistence.Entity;

import com.spaceprogram.simplejpa.model.IdedTimestampedBase;

@Entity
public class Script extends IdedTimestampedBase {
	private User owner;

	private String name;
	private String docurl;
	private String description;

	private boolean isdebug;
	private boolean ispublic;

	// S3 File names of various script formats
	private String original;
	private String compressed;
	private String minified;
	private String mincomp;

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
	public boolean isIsdebug() {
		return isdebug;
	}
	public void setIsdebug(boolean isdebug) {
		this.isdebug = isdebug;
	}
	public boolean isIspublic() {
		return ispublic;
	}
	public void setIspublic(boolean ispublic) {
		this.ispublic = ispublic;
	}
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}
	public String getCompressed() {
		return compressed;
	}
	public void setCompressed(String compressed) {
		this.compressed = compressed;
	}
	public String getMinified() {
		return minified;
	}
	public void setMinified(String minified) {
		this.minified = minified;
	}
	public String getMincomp() {
		return mincomp;
	}
	public void setMincomp(String mincomp) {
		this.mincomp = mincomp;
	}
}
