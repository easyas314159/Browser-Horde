package com.browserhorde.server.api.json;

import com.google.gson.annotations.Expose;

public class ResourceResponse extends ApiResponse {
	@Expose private final Object resource;

	public ResourceResponse(Object resource) {
		super(ApiResponseStatus.OK);

		this.resource = resource;
	}

	public Object getResource() {
		return resource;
	}
}
