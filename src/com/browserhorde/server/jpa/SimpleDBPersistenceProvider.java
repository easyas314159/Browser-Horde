package com.browserhorde.server.jpa;

import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;


public class SimpleDBPersistenceProvider implements PersistenceProvider {
	@Override
	public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map) {
		return new SimpleDBEntityManagerFactory(info, map);
	}

	@Override
	public EntityManagerFactory createEntityManagerFactory(String name, Map map) {
		return new SimpleDBEntityManagerFactory(name, map);
	}
}
