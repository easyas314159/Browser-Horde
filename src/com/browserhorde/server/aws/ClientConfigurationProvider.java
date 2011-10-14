package com.browserhorde.server.aws;

import com.amazonaws.ClientConfiguration;
import com.google.inject.Provider;

public class ClientConfigurationProvider implements Provider<ClientConfiguration> {
	@Override
	public ClientConfiguration get() {
		ClientConfiguration config = new ClientConfiguration();

		// TODO: Read in config

		return config;
	}
}
