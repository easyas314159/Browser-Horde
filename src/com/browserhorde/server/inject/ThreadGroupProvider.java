package com.browserhorde.server.inject;

import com.google.inject.Provider;

public class ThreadGroupProvider implements Provider<ThreadGroup> {
	private final String name;
	private final ThreadGroup parent;

	private boolean daemon = false;

	public ThreadGroupProvider(String name) {
		this(Thread.currentThread().getThreadGroup(), name);
	}
	public ThreadGroupProvider(ThreadGroup parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public ThreadGroupProvider withDaemon(boolean daemon) {
		this.daemon = daemon;
		return this;
	}

	@Override
	public ThreadGroup get() {
		ThreadGroup tg = new ThreadGroup(parent, name);
		tg.setDaemon(daemon);
		return tg;
	}
}
