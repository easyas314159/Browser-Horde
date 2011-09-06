package com.browserhorde.server.api.json;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;

@XmlRootElement(name="workorder")
public class WorkorderResponse implements ApiResponse {
	@Expose
	@XmlID
	@XmlAttribute
	private String id;

	@Expose
	@XmlElement
	private Date expires;

	@Expose
	@XmlElement
	private Task task;

	@Expose
	@XmlElement
	private JsonElement data;

	public WorkorderResponse() {
	}
	public WorkorderResponse(String id, Date expires, Task task, JsonElement data) {
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
