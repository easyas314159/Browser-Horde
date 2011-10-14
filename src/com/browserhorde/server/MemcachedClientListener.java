package com.browserhorde.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class MemcachedClientListener implements ServletContextListener {
	public static final String MEMCACHED_NAME = MemcachedClient.class.getName();

	private final Logger log = Logger.getLogger(getClass());

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		ServletContext context = evt.getServletContext();

		String connectionFactoryClassName = context.getInitParameter(ServletInitOptions.MEMCACHED_CONNECTION_FACTORY);
		connectionFactoryClassName = StringUtils.trimToNull(connectionFactoryClassName);

		ConnectionFactory connectionFactory = null;
		if(connectionFactoryClassName != null) {
			try {
				ClassLoader loader = ConnectionFactory.class.getClassLoader();
				Class<?> connectionFactoryClass = loader.loadClass(connectionFactoryClassName);
				connectionFactory = connectionFactoryClass
					.asSubclass(ConnectionFactory.class).newInstance();
			}
			catch(Throwable t) {
				log.warn("Failed to instantiate memcached connection factory", t);
			}
		}
		if(connectionFactory == null) {
			log.info("Using default memcached connection factory");
			connectionFactory = new DefaultConnectionFactory();
		}

		List<InetSocketAddress> clusterAddress = null;
		String cluster = context.getInitParameter(ServletInitOptions.MEMCACHED_CLUSTER);
		cluster = StringUtils.trimToNull(cluster);
		if(cluster == null) {
			log.warn("No Memcached cluster specified");
			return;
		}

		// FIXME: This may throw runtime exceptions
		clusterAddress = AddrUtil.getAddresses(cluster);

		if(clusterAddress == null || clusterAddress.size() == 0) {		
			log.warn("No memcached cluster specified; memcached will be disabled");
		}
		else {
			try {
				MemcachedClient memcached = new MemcachedClient(connectionFactory, clusterAddress);
				context.setAttribute(MEMCACHED_NAME, memcached);
			} catch(IOException ex) {
				log.error("Unable to create memcached client", ex);
			}
		}
	}
	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		ServletContext context = evt.getServletContext();
		MemcachedClient memcached = (MemcachedClient)context.getAttribute(MEMCACHED_NAME);
		context.setAttribute(MEMCACHED_NAME, null);

		memcached.shutdown();
	}
}
