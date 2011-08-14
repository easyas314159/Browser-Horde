package com.browserhorde.server.entity.json;

import java.lang.reflect.Type;

import com.browserhorde.server.entity.Script;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ScriptSerializer implements JsonSerializer<Script> {
	@Override
	public JsonElement serialize(Script o, Type type, JsonSerializationContext ctx) {
		JsonObject el = new JsonObject();

		el.addProperty("id", o.getId());
		el.add("created", ctx.serialize(o.getCreated()));
		
		el.add("user", ctx.serialize(o.getOwner()));

		el.addProperty("name", o.getName());
		el.addProperty("description", o.getDescription());
		el.addProperty("doc_url", o.getDocurl());

		el.addProperty("shared", o.isShared());
		el.addProperty("debug", o.isDebug());

		return el;
	}
}
