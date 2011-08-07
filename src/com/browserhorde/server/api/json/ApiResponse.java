package com.browserhorde.server.api.json;

import com.browserhorde.server.api.ApiResponseStatus;

public class ApiResponse {
	private final ApiResponseStatus status;

	public ApiResponse(ApiResponseStatus status) {
		this.status = status;
	}

	public ApiResponseStatus getStatus() {
		return status;
	}
}
