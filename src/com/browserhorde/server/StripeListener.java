package com.browserhorde.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.stripe.Stripe;

public class StripeListener implements ServletContextListener {
	private final Logger log = Logger.getLogger(getClass());

	@Override
	public void contextInitialized(ServletContextEvent evt) {
		ServletContext context = evt.getServletContext();

		String apiKey = context.getInitParameter(ServletInitOptions.STRIPE_API_KEY);
		if(apiKey == null) {
			log.warn("No Stripe API key supplied: billing is disabled");
		}
		else {
			Stripe.apiKey = apiKey;
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent evt) {
		Stripe.apiKey = null;
	}
}
