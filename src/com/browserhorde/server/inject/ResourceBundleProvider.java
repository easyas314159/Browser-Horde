package com.browserhorde.server.inject;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ResourceBundleProvider implements Provider<ResourceBundle> {
	private final Locale locale;

	@Inject
	public ResourceBundleProvider(HttpServletRequest req) {
		this.locale = req.getLocale();
	}

	@Override
	public ResourceBundle get() {
		return ResourceBundle.getBundle("bundle", locale);
	}

}
