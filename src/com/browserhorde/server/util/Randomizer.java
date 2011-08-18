package com.browserhorde.server.util;

import java.util.Random;

import org.apache.commons.codec.binary.Base64;

public class Randomizer {
	final int bytes;

	final ThreadLocal<Random> threadStreams;
	final ThreadLocal<byte[]> threadBuffers;

	public Randomizer(int bytes) {
		this.bytes = bytes;

		this.threadStreams = new ThreadLocal<Random>();
		this.threadBuffers = new ThreadLocal<byte[]>();
	}

	public synchronized String nextRandomizer() {
		byte buffer[];
		Random stream = null;

		synchronized(threadStreams) {
			stream = threadStreams.get();
			if(stream == null) {
				threadStreams.set(stream = new Random());
			}
			buffer = threadBuffers.get(); 
			if(buffer == null) {
				threadBuffers.set(buffer = new byte[bytes]);
			}
		}

		stream.nextBytes(buffer);
		return Base64.encodeBase64URLSafeString(buffer);
	}
}
