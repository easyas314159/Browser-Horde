package com.browserhorde.server.api.json;


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
