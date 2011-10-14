package com.browserhorde.server.queue;

import com.google.gson.annotations.Expose;

public class ProcessObject {
	@Expose private String bucket;
	@Expose private String source;
	
	public ProcessObject(String bucket, String source) {
		this.bucket = bucket;
		this.source = source;
	}

	public String getBucket() {
		return bucket;
	}
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getKey() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
