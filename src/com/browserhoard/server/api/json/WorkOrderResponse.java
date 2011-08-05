package com.browserhoard.server.api.json;

import com.browserhoard.server.api.ApiResponseStatus;

public class WorkOrderResponse extends ApiResponse {
	private final String id;

	public WorkOrderResponse(String id) {
		super(ApiResponseStatus.OK);

		this.id = id;
	}

	public String getId() {
		return id;
	}
}
