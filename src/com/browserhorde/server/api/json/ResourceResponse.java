package com.browserhorde.server.api.json;

public class ResourceResponse extends ApiResponse {
	private final Object resource;
	
	public ResourceResponse(Object resource) {
		super(ApiResponseStatus.OK);

		this.resource = resource;
	}

	public Object getResource() {
		return resource;
	}
}
