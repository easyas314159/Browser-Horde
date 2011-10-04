package com.browserhorde.server.gson;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class URLHandler implements JsonSerializer<URL>, JsonDeserializer<URL> {
	@Override
	public JsonElement serialize(URL o, Type type, JsonSerializationContext ctx) {
		return new JsonPrimitive(o.toString());
	}

	@Override
	public URL deserialize(JsonElement el, Type type, JsonDeserializationContext ctx) throws JsonParseException {
		String value = el.getAsString();
		if(value == null) {
			return null;
		}

		try {
			return new URL(value);
		} catch(MalformedURLException e) {
			throw new JsonParseException(e);
		}
	}

}
