package com.browserhorde.server.api.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amazonaws.services.simpledb.model.Attribute;

public class SimpleObject {
	private final String id;

	private final Map<String, String> attributes;

	public SimpleObject(String itemName, List<Attribute> attributes) {
		this.id = itemName;
		this.attributes = new HashMap<String, String>();
		for(Attribute attribute : attributes) {
			this.attributes.put(attribute.getName(), attribute.getValue());
		}
	}
	public String getId() {
		return id;
	}
	public String getAttribute(String name) {
		return attributes.get(name);
	}
	public Set<String> getAttributeNames() {
		return attributes.keySet();
	}
}
