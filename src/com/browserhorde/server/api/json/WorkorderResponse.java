package com.browserhorde.server.api.json;

import java.util.Date;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class WorkorderResponse extends ApiResponse {
	@Expose private final String id;
	@Expose private final Date expires;
	@Expose private final Task task;

	@Expose private final JsonElement data;

	public WorkorderResponse(String id, Date expires, Task task, JsonElement data) {
		super(ApiResponseStatus.OK);

		this.id = id;
		this.expires = expires;
		this.task = task;
		this.data = data;
	}

	public String getId() {
		return id;
	}
	public Date getExpires() {
		return expires;
	}
	public Task getTask() {
		return task;
	}
	public JsonElement getData() {
		return data;
	}
}
