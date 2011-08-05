package com.browserhoard.server.api.json;

import com.browserhoard.server.api.ApiResponseStatus;

public class ApiResponse {
	private final ApiResponseStatus status;

	public ApiResponse(ApiResponseStatus status) {
		this.status = status;
	}

	public ApiResponseStatus getStatus() {
		return status;
	}
}
