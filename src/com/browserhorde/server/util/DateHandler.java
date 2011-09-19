package com.browserhorde.server.util;

import java.lang.reflect.Type;
import java.util.Date;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateHandler implements JsonSerializer<Date>, JsonDeserializer<Date> {
	@Override
	public JsonElement serialize(Date o, Type type, JsonSerializationContext ctx) {
		return new JsonPrimitive(o.getTime());
	}

	@Override
	public Date deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		Long t = el.getAsLong();
		return t == null ? null : new Date(t);
	}

}
