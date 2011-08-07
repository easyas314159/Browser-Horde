package com.browserhorde.server.config;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.browserhorde.server.Configurator;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;

public class InitAmazonWebServices extends ConfigCommand {

	@Override
	public boolean execute(ConfigContext ctx) throws Exception {
		ServletContext context = ctx.getServletContext();

		context.setAttribute(Configurator.AWS_CREDENTIALS, initCredentials(context));
		context.setAttribute(Configurator.AWS_CLIENT_CONFIG, initClientConfig(context));

		return false;
	}
	
	private AWSCredentials initCredentials(ServletContext context) {
		String awsAccessKey = context.getInitParameter(ServletInitOptions.AWS_ACCESS_KEY);
		String awsSecretKey = context.getInitParameter(ServletInitOptions.AWS_SECRET_KEY);

		return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
	}
	private ClientConfiguration initClientConfig(ServletContext context) {
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
}
