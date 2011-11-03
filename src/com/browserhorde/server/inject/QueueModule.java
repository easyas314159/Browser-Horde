package com.browserhorde.server.inject;

import java.lang.annotation.Annotation;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class QueueModule extends AbstractModule {

	@Override
	protected void configure() {
		bindQueue(QueueGZIP.class, "Gzip");
		bindQueue(QueueMinify.class, "Minify");
		bindQueue(QueueBilling.class, "Billing");
		bindQueue(QueueDelete.class, "Delete");
		bindQueue(QueueStats.class, "Stats");
	}

	private void bindQueue(Class<? extends Annotation> a, String qn) {
		bind(String.class)
			.annotatedWith(a)
			.toProvider(new QueueUrlProvider(qn))
			.in(Singleton.class);
	}
}
