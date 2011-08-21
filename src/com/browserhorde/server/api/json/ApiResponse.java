package com.browserhorde.server.api.json;

import com.google.gson.annotations.Expose;


public class ApiResponse {
	@Expose private final ApiResponseStatus status;

	public ApiResponse(ApiResponseStatus status) {
		this.status = status;
	}

	public ApiResponseStatus getStatus() {
		return status;
	}
}
