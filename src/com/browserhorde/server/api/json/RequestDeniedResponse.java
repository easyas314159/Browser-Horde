package com.browserhorde.server.api.json;


public class RequestDeniedResponse extends ErrorResponse {
	public RequestDeniedResponse() {
		this(null);
	}
	public RequestDeniedResponse(String message) {
		super(ApiResponseStatus.REQUEST_DENIED, message);
	}
}
