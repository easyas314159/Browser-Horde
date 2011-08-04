package com.browserhoard.server;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.browserhoard.server.util.GsonUtils;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	public static final String MEMCAHED = "memcached";
	public static final String AWS_CREDENTIALS = "aws_credentials";

	public void contextInitialized(ServletContextEvent event) {
		try {
			ServletContext context = event.getServletContext();

			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			
			GsonUtils.getGsonBuilder()
				;

			initMemcached(context);
			initAWSCredentials(context);
			initAmazonS3(context);
		}
		catch(Throwable t) {
			log.error("Uh Oh!", t);
		}
	}

	public void contextDestroyed(ServletContextEvent eventt) {
	}

	private MemcachedClient initMemcached(ServletContext context) {
		ClassLoader loader = ConnectionFactory.class.getClassLoader();
		String connectionFactoryClass = StringUtils
				.trimToNull(context
						.getInitParameter(ServletInitOptions.MEMCACHED_CONNECTION_FACTORY));

		ConnectionFactory connectionFactory = null;
		if(connectionFactoryClass != null) {
			try {
				connectionFactory = loader.loadClass(connectionFactoryClass)
						.asSubclass(ConnectionFactory.class).newInstance();
			} catch(Throwable t) {
				log.warn("Failed to instantiate memcached connection factory", t);
			}
		}
		if(connectionFactory == null) {
			log.info("Using default memcached connection factory");
			connectionFactory = new DefaultConnectionFactory();
		}

		MemcachedClient memcached = null;
		String cluster = StringUtils.trimToNull(context
				.getInitParameter(ServletInitOptions.MEMCACHED_CLUSTER));
		if(cluster == null) {
			log.warn("No memcached cluster specified; memcached will be disabled");
		} else {
			try {
				List<InetSocketAddress> addresses = AddrUtil
						.getAddresses(cluster);
				memcached = new MemcachedClient(connectionFactory, addresses);
				context.setAttribute(Configurator.MEMCAHED, memcached);
			} catch(Throwable t) {
				log.error("Unable to create memcached client", t);
			}
		}
		return memcached;
	}

	private AWSCredentials initAWSCredentials(ServletContext context) {
		String awsAccessKey = context.getInitParameter(ServletInitOptions.AWS_ACCESS_KEY);
		String awsSecretKey = context.getInitParameter(ServletInitOptions.AWS_SECRET_KEY);

		AWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
		context.setAttribute(AWS_CREDENTIALS, credentials);

		return credentials;
	}

	private AmazonS3 initAmazonS3(ServletContext context) {
		AWSCredentials awsCredentials = (AWSCredentials)context.getAttribute(AWS_CREDENTIALS);
		AmazonS3 s3 = new AmazonS3Client(awsCredentials);

		List<Bucket> buckets = s3.listBuckets();
		for(Bucket bucket : buckets) {
			log.debug(bucket.getName());
		}

		return s3;
	}
}
