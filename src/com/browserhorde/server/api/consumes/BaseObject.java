package com.browserhorde.server.api.consumes;

import java.util.HashMap;

class BaseObject extends HashMap<String, Object> {
	public String getString(String key) {
		Object o = get(key);
		return (o instanceof String) ? (String)o : null;
	}

	public Integer getInteger(String key) {
		Object o = get(key);
		return (o instanceof Integer) ? (Integer)o : null;
	}

	public Boolean getBoolean(String key) {
		Object o = get(key);
		return (o instanceof Boolean) ? (Boolean)o : null;
	}
}
