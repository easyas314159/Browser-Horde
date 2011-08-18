package com.browserhorde.server.inject;

import org.apache.log4j.Logger;

import com.browserhorde.server.util.Randomizer;
import com.google.inject.Provider;

public class RandomizerProvider implements Provider<Randomizer> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public Randomizer get() {
		return new Randomizer(8);
	}
}
