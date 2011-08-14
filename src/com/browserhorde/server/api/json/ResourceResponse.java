package com.browserhorde.server.api.json;

public class ResourceResponse extends ApiResponse {
	private Object resource;
	
	public ResourceResponse(Object resource) {
		super(ApiResponseStatus.OK);

		this.resource = resource;
	}

	public Object getResource() {
		return resource;
	}
	public void setResource(Object resource) {
		this.resource = resource;
	}
}
