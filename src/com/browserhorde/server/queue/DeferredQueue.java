package com.browserhorde.server.queue;

import java.util.concurrent.ExecutorService;

public class DeferredQueue {
	private final ExecutorService executor;

	public DeferredQueue(ExecutorService executor) {
		this.executor = executor;
	}

	public void push(DeferredAction action) {
		executor.submit(new DeferredActionHandler(executor, action));
	}
}
