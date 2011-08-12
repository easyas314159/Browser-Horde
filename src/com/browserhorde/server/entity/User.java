package com.browserhorde.server.entity;

import javax.persistence.Entity;

import com.spaceprogram.simplejpa.model.IdedTimestampedBase;

@Entity
public class User extends IdedTimestampedBase {
	private String email;
	private String password;

	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
