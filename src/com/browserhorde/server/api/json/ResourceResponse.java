package com.browserhorde.server.api.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.Expose;

@XmlRootElement(name="resource")
public class ResourceResponse implements ApiResponse {
	@Expose
	@XmlElement
	private final Object resource;

	public ResourceResponse() {
		this(null);
	}
	public ResourceResponse(Object resource) {
		this.resource = resource;
	}
	public Object getResource() {
		return resource;
	}
}
