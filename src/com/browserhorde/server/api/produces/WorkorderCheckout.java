package com.browserhorde.server.api.produces;

import java.util.Date;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

public class WorkorderCheckout {
	@Expose
	private String id;

	@Expose
	private Date expires;

	@Expose
	private Task task;

	@Expose
	private JsonElement data;

	public WorkorderCheckout() {
	}
	public WorkorderCheckout(String id, Date expires, Task task, JsonElement data) {
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
