package com.browserhorde.server.util;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonElementSerializer implements JsonSerializer<JsonElement> {
	@Override
	public JsonElement serialize(JsonElement el, Type type, JsonSerializationContext ctx) {
		return el;
	}
}
