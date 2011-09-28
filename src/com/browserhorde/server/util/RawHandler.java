package com.browserhorde.server.util;

import java.lang.reflect.Type;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RawHandler implements JsonDeserializer<byte[]>, JsonSerializer<byte[]> {
	@Override
	public byte[] deserialize(JsonElement el, Type t, JsonDeserializationContext ctx) throws JsonParseException {
		String data = el.getAsString();
		if(data == null) {
			return null;
		}
		return Base64.decodeBase64(data);
	}

	@Override
	public JsonElement serialize(byte[] data, Type t, JsonSerializationContext ctx) {
		return new JsonPrimitive(Base64.encodeBase64String(data));
	}

}
