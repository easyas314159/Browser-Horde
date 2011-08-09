package com.browserhorde.server.config;

import javax.servlet.ServletContext;

import com.amazonaws.ClientConfiguration;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ClientConfigurationProvider implements Provider<ClientConfiguration> {
	@Inject private ServletContext context;

	@Override
	public ClientConfiguration get() {
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
