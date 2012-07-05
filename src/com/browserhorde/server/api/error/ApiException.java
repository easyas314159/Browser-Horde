package com.browserhorde.server.api.error;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

import com.google.gson.annotations.Expose;

public class ApiException extends WebApplicationException {
	private final StatusType status;

	@Expose
	private final String message;

	// TODO: Add ability to attach multiple processing errors to the response

	public ApiException(StatusType status) {
		this(status, null);
	}
	public ApiException(StatusType status, String message) {
		this.status = status;
		this.message = message;
	}
	
	@Override
	public Response getResponse() {
		return Response
			.status(status)
			.entity(this)
			.build()
			;
	}
}
