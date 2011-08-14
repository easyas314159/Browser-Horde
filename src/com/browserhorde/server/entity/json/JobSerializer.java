package com.browserhorde.server.entity.json;

import java.lang.reflect.Type;

import com.browserhorde.server.entity.Job;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JobSerializer implements JsonSerializer<Job> {
	@Override
	public JsonElement serialize(Job o, Type type, JsonSerializationContext ctx) {
		JsonObject el = new JsonObject();

		el.addProperty("id", o.getId());
		el.add("created", ctx.serialize(o.getCreated()));

		el.add("user", ctx.serialize(o.getOwner()));
		el.addProperty("name", o.getName());
		el.addProperty("description", o.getDescription());

		el.addProperty("website", o.getWebsite());
		el.addProperty("callback", o.getCallback());

		el.addProperty("public", o.isIspublic());
		el.addProperty("active", o.isIsactive());

		el.addProperty("timeout", o.getTimeout());

		el.add("script", ctx.serialize(o.getScript()));

		return el;
	}
}
