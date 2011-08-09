package com.browserhorde.server.api.json;


public class NoTasksResponse extends ErrorResponse {
	public NoTasksResponse() {
		this(null);
	}
	public NoTasksResponse(String message) {
		super(ApiResponseStatus.NO_TASKS, message);
	}
}
