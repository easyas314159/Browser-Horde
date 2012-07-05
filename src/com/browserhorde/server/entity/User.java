package com.browserhorde.server.entity;

import java.security.Principal;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import com.browserhorde.server.gson.Visibility;
import com.browserhorde.server.gson.VisibilityLevel;
import com.browserhorde.server.security.BCrypt;
import com.google.gson.annotations.Expose;

@Entity
@Visibility(VisibilityLevel.PRIVATE)
public class User extends BaseObject implements Principal {
	@Expose
	@Visibility(VisibilityLevel.PERSONAL)
	private String email;

	private String hash;

	@Expose
	private String name;

	@Expose
	private String bio;

	@Expose
	private String url;

	@Expose
	private String location;

	@Expose
	private int karma;

	@OneToMany
	@Basic(fetch=FetchType.LAZY)
	private Set<UserBilling> cards;

	@OneToMany
	@Basic(fetch=FetchType.LAZY)
	private Set<UserSocial> social;

	@OneToMany
	@Basic(fetch=FetchType.LAZY)
	private Set<Script> scripts;

	@OneToMany
	@Basic(fetch=FetchType.LAZY)
	private Set<Project> projects;

	@Override
	@Transient
	public String getName() {
		return getId();
	}

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}

	public boolean matchesPassword(String password) {
		return BCrypt.checkpw(password, getHash());
	}

	public void setPassword(String newPassword, String salt) {
		setHash(BCrypt.hashpw(newPassword, salt));
	}
	public boolean setPassword(String oldPassword, String newPassword, String salt) {
		if(matchesPassword(oldPassword)) {
			setPassword(newPassword, salt);
			return true;
		}
		return false;
	}

	public Set<Script> getScripts() {
		return scripts;
	}
	public Set<Project> getProjects() {
	}

	@Override
	public boolean isOwnedBy(User user) {
		return StringUtils.equals(user.getId(), getId());
	}
}
