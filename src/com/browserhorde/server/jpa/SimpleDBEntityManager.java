package com.browserhorde.server.jpa;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;

@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class SimpleDBEntityManager implements EntityManager {
	private SimpleDBEntityManagerFactory factory = null;

	private volatile boolean closed = false;

	public SimpleDBEntityManager(SimpleDBEntityManagerFactory factory, Map map) {
		this.factory = factory;
	}

	@Override
	public void clear() {
	}

	@Override
	public void close() {
		closed = true;
	}

	@Override
	public boolean contains(Object arg0) {
		return false;
	}

	@Override
	public Query createNamedQuery(String arg0) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0, Class arg1) {
		return null;
	}

	@Override
	public Query createNativeQuery(String arg0, String arg1) {
		return null;
	}

	@Override
	public Query createQuery(String arg0) {
		return null;
	}

	@Override
	public <T> T find(Class<T> clazz, Object id) {
		if(closed) {
			throw new PersistenceException("EntityManager already closed.");
		}
		if(id == null) {
			throw new IllegalArgumentException("Id value must not be null.");
		}
		try {
			
			// TODO: Check Cache
			return getItemById(clazz, id);
		}
		catch(AmazonClientException ex) {
			throw new PersistenceException(ex);
		}
	}
	public <T> Future<T> findAsync(final Class<T> clazz, final Object id) {
		if(factory.getExecutorService() == null) {
			throw new PersistenceException("ExecutorService unavailable");
		}
		return factory.getExecutorService().submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				return find(clazz, id);
			}
		});
	}

	@Override
	public void flush() {
	}

	@Override
	public Object getDelegate() {
		return null;
	}

	@Override
	public FlushModeType getFlushMode() {
		return null;
	}

	@Override
	public <T> T getReference(Class<T> arg0, Object arg1) {
		return null;
	}

	@Override
	public EntityTransaction getTransaction() {
		return null;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public void joinTransaction() {
	}

	@Override
	public void lock(Object arg0, LockModeType arg1) {
	}

	@Override
	public <T> T merge(T arg0) {
		return null;
	}

	@Override
	public void persist(Object o) {
	}

	@Override
	public void refresh(Object arg0) {
	}

	@Override
	public void remove(Object o) {
		if(closed) {
			throw new PersistenceException("");
		}
		if(o == null) {
			return;
		}

		/*
		// TODO: Check if domain exists
		String domainName = getDomainName(clazz);
		DeleteAttributesRequest request = new DeleteAttributesRequest()
			.withDomainName(domainName)
			.withItemName(getId(o));
		*/
	}

	@Override
	public void setFlushMode(FlushModeType arg0) {
	}

	public String getDomainName(Class<? extends Object> clazz) {
		return factory.getDomainName(clazz);
	}
	private boolean domainExists(String domainName) {
		return false;
	}

	private String getId(Object o) {
		return null;
	}
	
	private <T> T getItemById(Class<T> clazz, Object id) {
		return getItemById(clazz, id, false);
	}
	private <T> T getItemById(Class<T> clazz, Object id, boolean consistentRead) {
		String domainName = getDomainName(clazz);
		if(domainName == null) {
			return null;
		}

		GetAttributesRequest request = new GetAttributesRequest()
			.withDomainName(domainName)
			.withItemName(id.toString())
			.withConsistentRead(consistentRead)
			;
		GetAttributesResult result = factory.getSimpleDB().getAttributes(request);
		return buildEntityAndCache(clazz, id, result.getAttributes());
	}

	private <T> T buildEntityAndCache(Class<T> clazz, Object id, List<Attribute> attributes) {
		if(attributes == null || attributes.size() == 0) {
			return null;
		}
		T entity = buildEntity(clazz, id, attributes);
		// TODO: cache entity
		return entity;
	}
	private <T> T buildEntity(Class<T> clazz, Object id, List<Attribute> attributes) {
		return null;
	}
}
