package com.browserhorde.server.api.json;

import com.google.gson.annotations.Expose;


public class ErrorResponse extends ApiResponse {
	@Expose private final String message;

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
