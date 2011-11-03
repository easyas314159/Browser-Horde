package com.browserhorde.server.api.consumes;

import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.readers.JsonReader;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class WorkorderCheckin {
	@Expose private String id;
	@Expose private String task;

	@Expose private WorkorderCheckinStatus status;

	@Expose private JsonElement data;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getTask() {
		return task;
	}
	public void setTask(String task) {
		this.task = task;
	}

	public WorkorderCheckinStatus getStatus() {
		return status;
	}
	public void setStatus(WorkorderCheckinStatus status) {
		this.status = status;
	}

	public JsonElement getData() {
		return data;
	}
	public void setData(JsonElement data) {
		this.data = data;
	}

	@Provider
	public static final class Reader extends JsonReader<WorkorderCheckin> {}
}
