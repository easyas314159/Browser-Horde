package com.browserhorde.server.api.json;

import com.browserhorde.server.api.ApiResponseStatus;

public class NoTasksResponse extends ErrorResponse {
	public NoTasksResponse() {
		this(null);
	}
	public NoTasksResponse(String message) {
		super(ApiResponseStatus.NO_TASKS, message);
	}
}
