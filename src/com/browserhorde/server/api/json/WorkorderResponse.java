package com.browserhorde.server.api.json;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class WorkorderResponse extends ApiResponse {
	@Expose private final String id;
	@Expose private final Task task;

	@Expose private final JsonElement data;

	public WorkorderResponse(String id, Task task, JsonElement data) {
		super(ApiResponseStatus.OK);

		this.id = id;
		this.task = task;
		this.data = data;
	}

	public String getId() {
		return id;
	}
	public Task getTask() {
		return task;
	}
	public JsonElement getData() {
		return data;
	}
}
