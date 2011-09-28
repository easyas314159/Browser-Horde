package com.browserhorde.server.api.consumes;

import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.readers.JsonReader;
import com.google.gson.annotations.Expose;

public class ModifyScriptRequest {
	@Expose public String name;
	@Expose public String desccription;
	@Expose public Boolean debug;

	@Provider
	public static final class Reader extends JsonReader<ModifyScriptRequest> {}
}
