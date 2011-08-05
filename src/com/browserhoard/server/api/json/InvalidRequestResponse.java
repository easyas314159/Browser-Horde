package com.browserhoard.server.api.json;

import com.browserhoard.server.api.ApiResponseStatus;

public class InvalidRequestResponse extends ErrorResponse {
	public InvalidRequestResponse() {
		this(null);
	}
	public InvalidRequestResponse(String message) {
		super(ApiResponseStatus.INVALID_REQUEST, message);
	}
}
