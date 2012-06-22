package com.browserhorde.server;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.stripe.Stripe;

public class StripeListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent evt) {
		ServletContext context = evt.getServletContext();

		String apiKey = context.getInitParameter(ServletInitOptions.STRIPE_API_KEY);
		if(apiKey == null) {
			// TODO: Log billing disabled
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
