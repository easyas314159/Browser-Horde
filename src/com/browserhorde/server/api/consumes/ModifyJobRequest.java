package com.browserhorde.server.api.consumes;

import java.net.URL;

import javax.ws.rs.ext.Provider;

import com.browserhorde.server.api.readers.JsonReader;
import com.google.gson.annotations.Expose;

public class ModifyJobRequest {
	@Expose public String owner;
	@Expose public String name;
	@Expose public String description;
	@Expose public String script;
	@Expose public URL website;
	@Expose public URL callback;

	@Expose public Boolean active;

	@Expose public Integer timeout;

	@Provider
	public static final class Reader extends JsonReader<ModifyJobRequest> {}
}
