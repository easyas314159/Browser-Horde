package com.browserhorde.server;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.browserhorde.server.aws.BucketManager;
import com.browserhorde.server.aws.DomainManager;
import com.browserhorde.server.util.GsonUtils;
import com.browserhorde.server.util.ParamUtils;

public class Configurator implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	// Thread Pool
	public static final String THREAD_POOL = "thread_pool";
	
	// Memcached
	public static final String MEMCAHED = "memcached";

	// Amazon Web Services
	public static final String AWS_CREDENTIALS = "aws_credentials";
	public static final String AWS_CLIENT_CONFIG = "aws_client_configuration";
	
	public static final String AWS_S3 = "aws_s3";
	public static final String AWS_SIMPLEDB = "aws_simpledb";

	public void contextInitialized(ServletContextEvent event) {
		try {
			ServletContext context = event.getServletContext();

			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			
			GsonUtils.getGsonBuilder()
				;

			// Thread Pool
			ScheduledThreadPoolExecutor executor = initThreadPool(context);
			context.setAttribute(THREAD_POOL, executor);

			// Memcached
			MemcachedClient memcached = initMemcached(context);
			context.setAttribute(MEMCAHED, memcached);

			// Amazon Web Services
			AWSCredentials awsCredentials = initAWSCredentials(context);
			ClientConfiguration awsClientConfig = initAWSClientConfig(context);

			AmazonS3 awsS3 = initAmazonS3(context, awsCredentials, awsClientConfig);
			AmazonSimpleDBAsync awsSimpleDB = initAmazonSimpleDB(context, awsCredentials, awsClientConfig, executor);

			context.setAttribute(AWS_CREDENTIALS, awsCredentials);
			context.setAttribute(AWS_CLIENT_CONFIG, awsClientConfig);

			context.setAttribute(AWS_S3, awsS3);
			context.setAttribute(AWS_SIMPLEDB, awsSimpleDB);
		}
		catch(Throwable t) {
			log.error("Uh Oh!", t);
		}
	}

	public void contextDestroyed(ServletContextEvent eventt) {
	}

	private ScheduledThreadPoolExecutor initThreadPool(ServletContext context) {
		Runtime rt = Runtime.getRuntime();

		// TODO: Externalize these options into context config
		int core_pool_size = 1;
		int max_pool_size = rt.availableProcessors() << 1;
		int keep_alive_timeout = 300;
		boolean allow_core_thread_timeout = false;

		if(max_pool_size < core_pool_size) {
			core_pool_size = max_pool_size;
		}
		if(keep_alive_timeout == 0 && allow_core_thread_timeout) {
			allow_core_thread_timeout = false;
		}

		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(core_pool_size);

		executor.setKeepAliveTime(keep_alive_timeout, TimeUnit.SECONDS);
		executor.allowCoreThreadTimeOut(allow_core_thread_timeout);
		executor.setMaximumPoolSize(max_pool_size);

		// TODO: Make this gracefully log any rejections
		//executor.setRejectedExecutionHandler(handler);

		// TODO: Allow custom thread factory configuration
		//executor.setThreadFactory(threadFactory);

		return executor;
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

		return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	}
	private ClientConfiguration initAWSClientConfig(ServletContext context) {
		ClientConfiguration config = new ClientConfiguration();

		config.setUserAgent(
				ParamUtils.asString(
						context.getInitParameter(ServletInitOptions.AWS_USER_AGENT),
						config.getUserAgent()
					)
				);

		// TODO: Read in config

		return config;
	}

	private AmazonS3 initAmazonS3(ServletContext context, AWSCredentials awsCredentials, ClientConfiguration awsClientConfig) {
		String s3BucketPrefix = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_S3_BUCKET_PREFIX));
		String s3Buckets = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_S3_BUCKETS));

		BucketManager.setBucketPrefix(s3BucketPrefix);
		AmazonS3 s3 = new AmazonS3Client(awsCredentials, awsClientConfig);

		Set<String> newBuckets = new HashSet<String>();
		String rawBuckets[] = s3Buckets.split(";");

		for(String bucket : rawBuckets) {
			bucket = StringUtils.trimToNull(bucket);
			if(bucket != null) {
				bucket = BucketManager.getBucket(bucket);
				newBuckets.add(bucket);
			}
		}

		List<Bucket> buckets = s3.listBuckets();
		for(Bucket bucket : buckets) {
			newBuckets.remove(bucket.getName());
		}
		for(String bucket : newBuckets) {
			log.info(String.format("Creating bucket: \'%s\'", bucket));
			s3.createBucket(bucket, Region.US_Standard);
		}

		return s3;
	}
	
	private AmazonSimpleDBAsync initAmazonSimpleDB(ServletContext context, AWSCredentials awsCredentials, ClientConfiguration awsClientConfig, ExecutorService executor) {
		String sdbDomainPrefix = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAIN_PREFIX));
		String sdbDomains = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAINS));

		DomainManager.setDomainPrefix(sdbDomainPrefix);
		AmazonSimpleDBAsync sdb = new  AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, executor);

		Set<String> newDomains = new HashSet<String>();
		String rawDomains[] = sdbDomains.split(";");

		for(String domain : rawDomains) {
			domain = StringUtils.trimToNull(domain);
			if(domain != null) {
				domain = DomainManager.getDomain(domain);
				newDomains.add(domain);
			}
		}

		ListDomainsResult domainResult = null;
		ListDomainsRequest domainRequest = new ListDomainsRequest();
		do {
			if(domainResult != null) {
				domainRequest.setNextToken(domainResult.getNextToken());
			}
			domainResult = sdb.listDomains(domainRequest);
			newDomains.removeAll(domainResult.getDomainNames());
		} while(domainResult.getNextToken() != null);

		List<Future<Void>> createFutures = new LinkedList<Future<Void>>();
		for(String domain : newDomains) {
			log.info(String.format("Creating domain \'%s\'", domain));
			CreateDomainRequest createRequest = new CreateDomainRequest(domain);
			createFutures.add(sdb.createDomainAsync(createRequest));
		}
		for(Future<Void> future : createFutures) {
			try {
				future.get(5, TimeUnit.SECONDS);
			}
			catch(Throwable t) {
				log.error("Domain creation failed", t);
			}
		}

		return sdb;
	}
}
