package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class ScriptAccess extends BaseObject {
	@ManyToOne
	private User user;
	
	@ManyToOne
	private Script script;

	private int privileges;

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public Script getScript() {
		return script;
	}
	public void setScript(Script script) {
		this.script = script;
	}

	public int getPrivileges() {
		return privileges;
	}
	public void setPrivileges(int privileges) {
		this.privileges = privileges;
	}
}
