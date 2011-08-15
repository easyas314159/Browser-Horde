package com.browserhorde.server.api.json;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;


public class WorkorderResponse extends ApiResponse {
	private final String id;
	private final Task task;

	private final JsonElement data;

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
