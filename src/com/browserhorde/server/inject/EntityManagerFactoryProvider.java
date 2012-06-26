package com.browserhorde.server.inject;

import javax.persistence.EntityManagerFactory;

import org.apache.log4j.Logger;

import com.google.inject.Provider;

public class EntityManagerFactoryProvider implements Provider<EntityManagerFactory> {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public EntityManagerFactory get() {

		EntityManagerFactory factory = null;
		try {
			// TODO: Create EntityManagerFactory
		}
		catch(Throwable t) {
			log.fatal("Failed to create EntityManagerFactory", t);
		}
		return factory;
	}
}
