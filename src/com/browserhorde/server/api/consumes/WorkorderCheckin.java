package com.browserhorde.server.api.consumes;

import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.readers.JsonReader;
import com.google.gson.annotations.Expose;

public class WorkorderCheckin {
	@Expose private String id;
	@Expose private String job;
	@Expose private String task;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getJob() {
		return job;
	}
	public void setJob(String job) {
		this.job = job;
	}

	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}

	@Provider
	public static final class Reader extends JsonReader<WorkorderCheckin> {}
}
