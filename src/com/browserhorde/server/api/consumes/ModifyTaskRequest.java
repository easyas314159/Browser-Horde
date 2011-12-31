package com.browserhorde.server.api.consumes;

import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.readers.JsonReader;
import com.google.gson.annotations.Expose;

public class ModifyTaskRequest {
	@Expose public Integer timeout;
	@Expose public Boolean active;

	@Provider
	public static final class Reader extends JsonReader<ModifyTaskRequest> {}
}
