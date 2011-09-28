package com.browserhorde.server.inject;

import com.amazonaws.ClientConfiguration;
import com.browserhorde.server.ServletInitOptions;
import com.browserhorde.server.util.ParamUtils;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

public class ClientConfigurationProvider implements Provider<ClientConfiguration> {
	@Inject @Named(ServletInitOptions.AWS_USER_AGENT) private String awsUserAgent;

	@Override
	public ClientConfiguration get() {
		ClientConfiguration config = new ClientConfiguration();

		config.setUserAgent(
				ParamUtils.asString(
						awsUserAgent,
						config.getUserAgent()
					)
				);

		// TODO: Read in config

		return config;
	}
}
