package com.browserhorde.server.inject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Vector;

import javax.servlet.ServletContext;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.services.elasticache.AmazonElastiCacheAsync;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.CacheClusterNotFoundException;
import com.amazonaws.services.elasticache.model.CacheNode;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersResult;
import com.amazonaws.services.elasticache.model.Endpoint;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class MemcachedClientProvider implements Provider<MemcachedClient> {
	private final Logger log = Logger.getLogger(getClass());

	@Inject private ServletContext context;
	@Inject private AmazonElastiCacheAsync elastiCache;

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
		List<InetSocketAddress> clusterAddress = null;
		String cluster = StringUtils.trimToNull(context.getInitParameter(ServletInitOptions.MEMCACHED_CLUSTER));
		if(cluster == null) {
			String clusterId = ParamUtils.asString(context.getInitParameter(ServletInitOptions.MEMCACHED_CLUSTER_ID));
			if(clusterId != null) {
				DescribeCacheClustersRequest describeReq = new DescribeCacheClustersRequest()
					.withShowCacheNodeInfo(true)
					.withCacheClusterId(clusterId)
					;
				try {
					DescribeCacheClustersResult describeResult = elastiCache.describeCacheClusters(describeReq);

					clusterAddress = new Vector<InetSocketAddress>();
					CacheCluster cacheCluster = describeResult.getCacheClusters().get(0);
					for(CacheNode cacheNode : cacheCluster.getCacheNodes()) {
						Endpoint endpoint = cacheNode.getEndpoint();
						clusterAddress.add(new InetSocketAddress(endpoint.getAddress(), endpoint.getPort()));
					}
				}
				catch(CacheClusterNotFoundException ex) {
					log.warn(String.format("Cluster \'%s\' does not exist", clusterId));
				}
			}
		}
		else {
			// FIXME: This may throw runtime exceptions
			clusterAddress = AddrUtil.getAddresses(cluster);
		}

		if(clusterAddress == null || clusterAddress.size() == 0) {		
			log.warn("No memcached cluster specified; memcached will be disabled");
		}
		else {
			try {
				memcached = new MemcachedClient(connectionFactory, clusterAddress);
			} catch(IOException ex) {
				log.error("Unable to create memcached client", ex);
			}
		}

		return memcached;
	}
}
