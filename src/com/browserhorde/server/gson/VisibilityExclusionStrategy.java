package com.browserhorde.server.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class VisibilityExclusionStrategy implements ExclusionStrategy {
	private final VisibilityLevel level;

	public VisibilityExclusionStrategy(VisibilityLevel level) {
		this.level = level;
	}

	@Override
	public boolean shouldSkipClass(Class<?> c) {
		return exclude(c.getAnnotation(Visibility.class));
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		return exclude(f.getAnnotation(Visibility.class));
	}

	private boolean exclude(Visibility annotation) {		
		if(annotation == null) {
			return false;
		}
		return level.compareTo(annotation.value()) < 0;
	}

}
