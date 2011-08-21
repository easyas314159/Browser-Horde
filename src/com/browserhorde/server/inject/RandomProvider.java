package com.browserhorde.server.inject;

import java.security.SecureRandom;
import java.util.Random;

import com.google.inject.Provider;

public class RandomProvider implements Provider<Random> {
	private static final ThreadLocal<Random> threadRandom = new ThreadLocal<Random>();

	@Override
	public Random get() {
		Random r = threadRandom.get();
		if(r == null) {
			threadRandom.set(r = new SecureRandom());
		}
		return r;
	}
}
