package com.browserhorde.server.inject;

import java.net.InetSocketAddress;
import java.util.List;

import javax.servlet.ServletContext;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.ServletInitOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MemcachedClientProvider implements Provider<MemcachedClient> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private ServletContext context;

	@Override
	public MemcachedClient get() {
		ClassLoader loader = ConnectionFactory.class.getClassLoader();
		String connectionFactoryClassName = StringUtils.trimToNull(context.getInitParameter(ServletInitOptions.MEMCACHED_CONNECTION_FACTORY));

		ConnectionFactory connectionFactory = null;
		if(connectionFactoryClassName != null) {
			try {
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

		MemcachedClient memcached = null;
		String cluster = StringUtils.trimToNull(context.getInitParameter(ServletInitOptions.MEMCACHED_CLUSTER));
		if(cluster == null) {
			log.warn("No memcached cluster specified; memcached will be disabled");
		}
		else {
			try {
				List<InetSocketAddress> addresses = AddrUtil.getAddresses(cluster);
				memcached = new MemcachedClient(connectionFactory, addresses);

				/*memcached.addObserver(new MemcachedMonitor());*/
			}
			catch(Throwable t) {
				log.error("Unable to create memcached client", t);
			}
		}
		return memcached;
	}
}
