package com.browserhorde.server.api.json;

import com.browserhorde.server.api.ApiResponseStatus;

public class ErrorResponse extends ApiResponse {
	private final String message;

	public ErrorResponse(ApiResponseStatus status) {
		this(status, null);
	}
	public ErrorResponse(ApiResponseStatus status, String message) {
		super(status);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}
}
