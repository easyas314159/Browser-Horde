package com.browserhorde.server.api.json;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class WorkorderResponseSerializer implements JsonSerializer<WorkorderResponse> {

	@Override
	public JsonElement serialize(WorkorderResponse o, Type type, JsonSerializationContext ctx) {
		JsonObject el = new JsonObject();

		el.addProperty("id", o.getId());
		el.add("task", ctx.serialize(o.getTask()));
		el.add("data", o.getData());

		return el;
	}

}
