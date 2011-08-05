package com.browserhoard.server.api.json;

import com.browserhoard.server.api.ApiResponseStatus;

public class NoTasksResponse extends ErrorResponse {
	public NoTasksResponse() {
		this(null);
	}
	public NoTasksResponse(String message) {
		super(ApiResponseStatus.NO_TASKS, message);
	}
}
