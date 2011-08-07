package com.browserhorde.server.config;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsync;
import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.ListDomainsRequest;
import com.amazonaws.services.simpledb.model.ListDomainsResult;
import com.browserhorde.server.Configurator;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.aws.DomainManager;
import com.browserhorde.server.util.ParamUtils;

public class InitAmazonSDB extends ConfigCommand {

	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		ServletContext context = ctx.getServletContext();

		AWSCredentials awsCredentials = (AWSCredentials)context.getAttribute(Configurator.AWS_CREDENTIALS);
		ClientConfiguration awsClientConfig = (ClientConfiguration)context.getAttribute(Configurator.AWS_CLIENT_CONFIG);
		ExecutorService executorService = (ExecutorService)context.getAttribute(Configurator.EXECUTOR_SERVICE);

		String sdbDomainPrefix = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAIN_PREFIX));
		String sdbDomains = ParamUtils.asString(context.getInitParameter(ServletInitOptions.AWS_SIMPLEDB_DOMAINS));

		DomainManager.setDomainPrefix(sdbDomainPrefix);
		AmazonSimpleDBAsync sdb = new  AmazonSimpleDBAsyncClient(awsCredentials, awsClientConfig, executorService);

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

		context.setAttribute(Configurator.AWS_SDB, sdb);
		
		return false;
	}

}
