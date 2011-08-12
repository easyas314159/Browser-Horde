package com.browserhorde.server.inject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class EntityManagerProvider implements Provider<EntityManager> {
	@Inject private EntityManagerFactory managerFactory;

	@Override
	public EntityManager get() {
		if(managerFactory == null) {
			return null;
		}
		return managerFactory.createEntityManager();
	}
}
