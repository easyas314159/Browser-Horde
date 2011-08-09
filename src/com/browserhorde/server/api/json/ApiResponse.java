package com.browserhorde.server.api.json;


public class ApiResponse {
	private final ApiResponseStatus status;

	public ApiResponse(ApiResponseStatus status) {
		this.status = status;
	}

	public ApiResponseStatus getStatus() {
		return status;
	}
}
