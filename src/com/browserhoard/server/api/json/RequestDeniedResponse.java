package com.browserhoard.server.api.json;

import com.browserhoard.server.api.ApiResponseStatus;

public class RequestDeniedResponse extends ErrorResponse {
	public RequestDeniedResponse() {
		this(null);
	}
	public RequestDeniedResponse(String message) {
		super(ApiResponseStatus.REQUEST_DENIED, message);
	}
}
