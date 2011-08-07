package com.browserhorde.server.config;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsync;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceAsyncClient;
import com.browserhorde.server.Configurator;

public class InitAmazonSES extends ConfigCommand {
	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		ServletContext context = ctx.getServletContext();

		AWSCredentials awsCredentials = (AWSCredentials)context.getAttribute(Configurator.AWS_CREDENTIALS);
		ClientConfiguration awsClientConfig = (ClientConfiguration)context.getAttribute(Configurator.AWS_CLIENT_CONFIG);
		ExecutorService executorService = (ExecutorService)context.getAttribute(Configurator.EXECUTOR_SERVICE);

		AmazonSimpleEmailServiceAsync ses = new AmazonSimpleEmailServiceAsyncClient(awsCredentials, awsClientConfig, executorService);
		context.setAttribute(Configurator.AWS_SES, ses);

		return false;
	}

}
