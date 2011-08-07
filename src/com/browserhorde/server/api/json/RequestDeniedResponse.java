package com.browserhorde.server.api.json;

import com.browserhorde.server.api.ApiResponseStatus;

public class RequestDeniedResponse extends ErrorResponse {
	public RequestDeniedResponse() {
		this(null);
	}
	public RequestDeniedResponse(String message) {
		super(ApiResponseStatus.REQUEST_DENIED, message);
	}
}
