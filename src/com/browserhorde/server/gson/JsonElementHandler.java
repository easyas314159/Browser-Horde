package com.browserhorde.server.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class JsonElementHandler implements JsonSerializer<JsonElement>, JsonDeserializer<JsonElement> {
	@Override
	public JsonElement serialize(JsonElement el, Type type, JsonSerializationContext ctx) {
		return el;
	}
	@Override
	public JsonElement deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		return el;
	}
}
