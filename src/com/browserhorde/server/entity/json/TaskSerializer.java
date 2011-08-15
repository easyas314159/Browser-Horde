package com.browserhorde.server.entity.json;

import java.lang.reflect.Type;

import com.browserhorde.server.entity.Task;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TaskSerializer implements JsonSerializer<Task> {
	@Override
	public JsonElement serialize(Task o, Type type, JsonSerializationContext ctx) {
		JsonObject el = new JsonObject();

		el.add("job", ctx.serialize(o.getJob()));
		el.addProperty("public", o.isIspublic());
		el.addProperty("active", o.isActive());
		el.addProperty("timeout", o.getTimeout());

		return el;
	}
}
