package com.browserhorde.server.queue;

import java.util.Collection;
import java.util.concurrent.ExecutorService;

class DeferredActionHandler implements Runnable {
	private final ExecutorService executor;
	private final DeferredAction action;

	private int maxAttempts;
	
	public DeferredActionHandler(ExecutorService executor, DeferredAction action) {
		this.executor = executor;
		this.action = action;
		
		maxAttempts = Math.max(1, action.getMaxAttempts());
	}

	@Override
	public void run() {
		--maxAttempts;

		try {
			action.execute();

			// TODO: Log success
			submitSubActions(action.getSuccessActions());
		}
		catch(Throwable t) {
			// TODO: Log Error

			if(maxAttempts > 0) {
				// TODO: Possibly use scheduling to increase odds of success
				executor.submit(this);
			}
			else {
				submitSubActions(action.getFailureActions());
			}
		}
	}

	private void submitSubActions(Collection<DeferredAction> actions) {
		for(DeferredAction action : actions) {
			executor.submit(new DeferredActionHandler(executor, action));
		}
	}
}
