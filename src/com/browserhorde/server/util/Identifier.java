package com.browserhorde.server.util;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public final class Identifier {
	private static final ThreadLocal<Random> threadRandomStream;
	
	static {
		threadRandomStream = new ThreadLocal<Random>();
	}

	private Identifier() {}

	private synchronized static Random getRandomStream() {
		Random randomStream = threadRandomStream.get();
		if(randomStream == null) {
			threadRandomStream.set(randomStream = new SecureRandom());
		}
		return randomStream;
	}

	public static String generate(int length) {
		byte data[] = new byte[length];
		Random randomStream = getRandomStream();

		randomStream.nextBytes(data);
		return Base64.encodeBase64URLSafeString(data);
	}
}
