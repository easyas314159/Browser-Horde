package com.browserhorde.server.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Entity
public class ScriptVersion extends BaseObject {
	@ManyToOne
	@Basic(fetch=FetchType.LAZY)
	@Column(nullable = false)
	private Script script;

	@ManyToOne
	@Basic(fetch=FetchType.LAZY)
	@Column(nullable = false)
	private User uploader;

	@Column(nullable = false)
	private String version;
	private String digest;

	private boolean deterministic;

	public Script getScript() {
		return script;
	}
	public void setScript(Script script) {
		this.script = script;
	}

	public User getUploader() {
		return uploader;
	}
	public void setUploader(User uploader) {
		this.uploader = uploader;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}

	public boolean isDeterministic() {
		return deterministic;
	}
	public void setDeterministic(boolean deterministic) {
		this.deterministic = deterministic;
	}
}
