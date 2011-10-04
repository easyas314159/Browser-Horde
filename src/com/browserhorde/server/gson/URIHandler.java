package com.browserhorde.server.gson;

import java.lang.reflect.Type;
import java.net.URI;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class URIHandler implements JsonSerializer<URI>, JsonDeserializer<URI> {
	@Override
	public JsonElement serialize(URI o, Type type, JsonSerializationContext ctx) {
		return new JsonPrimitive(o.toString());
	}

	@Override
	public URI deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		String value = el.getAsString();
		if(value == null) {
			return null;
		}

		return URI.create(value);
	}

}
