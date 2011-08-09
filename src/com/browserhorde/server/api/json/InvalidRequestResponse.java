package com.browserhorde.server.api.json;


public class InvalidRequestResponse extends ErrorResponse {
	public InvalidRequestResponse() {
		this(null);
	}
	public InvalidRequestResponse(String message) {
		super(ApiResponseStatus.INVALID_REQUEST, message);
	}
}
