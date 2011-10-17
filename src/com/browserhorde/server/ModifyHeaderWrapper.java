package com.browserhorde.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.iterators.IteratorEnumeration;

@SuppressWarnings("rawtypes")
public class ModifyHeaderWrapper extends HttpServletRequestWrapper {
	private Set<String> base;
	private Set<String> remove;
	private Map<String, List<String>> override;

	public ModifyHeaderWrapper(HttpServletRequest request) {
		super(request);

		base = new HashSet<String>();
		remove = new HashSet<String>();
		override = new HashMap<String, List<String>>();

		CollectionUtils.addAll(base, request.getHeaderNames());
	}

	public ModifyHeaderWrapper withHeader(String name, String value) {
		setHeader(name, value);
		return this;
	}

	public void addHeader(String name, String value) {
		if(value == null) {
			return;
		}

		remove.remove(name);
		name = name.toLowerCase();

		List<String> values = override.get(name);
		if(values == null) {
			override.put(name, values = new ArrayList<String>());
		}
		values.add(value);
	}
	public void setHeader(String name, String value) {
		name = name.toLowerCase();

		if(value == null) {
			remove.add(name);
			override.remove(name);
		}
		else {
			List<String> values = override.get(name);
			if(values == null) {
				override.put(name, values = new ArrayList<String>());
			}
			else {
				values.clear();
			}
			values.add(value);
		}
	}

	@Override
	public String getHeader(String name) {
		name = name.toLowerCase();
		Enumeration<String> e = getHeaders(name);
		if(e.hasMoreElements()) {
			return e.nextElement();
		}
		return null;
	}
	@Override
	public Enumeration getHeaders(String name) {
		name = name.toLowerCase();
		if(remove.contains(name)) {
			return new IteratorEnumeration();
		}
		if(override.containsKey(name)) {
			List<String> values = override.get(name);
			return new IteratorEnumeration(values.iterator());
		}
		else {
			return super.getHeaders(name);
		}
	}

	@Override
	public Enumeration getHeaderNames() {
		Collection<String> names = new HashSet<String>(base);

		names.removeAll(remove);
		names.addAll(override.keySet());

		return new IteratorEnumeration(names.iterator());
	}
}
