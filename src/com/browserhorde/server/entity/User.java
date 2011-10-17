package com.browserhorde.server.entity;

import java.security.Principal;

import javax.persistence.Entity;
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
	@Visibility(VisibilityLevel.PERSONAL)
	private String consumerKey;

	@Expose
	@Visibility(VisibilityLevel.PERSONAL)
	private String consumerSecret;

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

	public String getConsumerKey() {
		return consumerKey;
	}
	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}
	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}
	
	@Override
	public boolean isOwnedBy(User user) {
		return StringUtils.equals(user.getId(), getId());
	}
}
