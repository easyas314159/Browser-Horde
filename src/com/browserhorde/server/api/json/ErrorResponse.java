package com.browserhorde.server.api.json;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.gson.annotations.Expose;

@XmlRootElement(name="error")
public class ErrorResponse implements ApiResponse {
	@Expose
	@XmlElement
	private ErrorStatus status;

	@Expose
	@XmlElement
	private String message;

	public ErrorResponse() {
		this(ErrorStatus.UNKNOWN, null);
	}
	public ErrorResponse(ErrorStatus status) {
		this(status, null);
	}
	public ErrorResponse(String message) {
		this(null, message);
	}
	public ErrorResponse(ErrorStatus status, String message) {
		this.status = status;
		this.message = message;
	}
}
