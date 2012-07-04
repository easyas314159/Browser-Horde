package com.browserhorde.server.api.consumes;

import java.util.HashMap;

class BaseObject extends HashMap<String, Object> {
	public String getString(String key) {
		Object o = get(key);
		if(o instanceof String) {
			return (String)o;
		}
		return null;
	}
}
