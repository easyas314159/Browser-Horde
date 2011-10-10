/*
TODO: In hindsight we should only handle one task per environment
so if someone does compromise something it doesn't effect subsequent tasks
 */

onmessage = (function() {
    var dutyCycle = .90, isInitialized = false, executionTimer = null, taskScript = null, taskState = null, taskActive = null,

	handlers = {},

	// Store some functions for minification/sercurity purposes
	_importScripts = importScripts, _postMessage = postMessage, _setTimeout = setTimeout;

	// eval = null; // Eval is evil but uncommenting this line messes up
	// minification

	// Disable access to stuff people shouldn't be accessing
	setTimeout = null; // We don't want tasks executing stuff outside the normal bounds of execution
	importScripts = null; // We don't want tasks loading external scripts
	postMessage = null; // No posting to the parent process

	_importScripts('messaging.js');

	handlers[MSG.PRELOAD_SCRIPT] = function(id) {
		if (taskScript) {
			return;
		}

		$ = {
			setup : defaultSetup
		};

		// Prevent a malicious script from overwriting onmessage
		var onmessageStored = onmessage;
		_importScripts(apiPath + "/scripts/" + id + ".js");
		onmessage = onmessageStored;

		$.status = postStatus;
		$.log = postLog;

		if (!($.setup && $.iterate)) {
			sendMessage(MSG.PRELOAD_FAILED, null);
		} else {
			taskScript = $;
		}

		sendMessage(MSG.PRELOAD_COMPLETE, id);
	};
	handlers[MSG.SET_WORKORDER] = function(wo) {
		if (wo && wo.id) {
			if (taskActive) {
				if (taskActive.id && wo.id == taskActive.id) {
					return;
				}
				cancelTask();
			}
			isInitialized = false;
			taskActive = wo;
		}
	};
	handlers[MSG.GET_WORKORDER] = function(id) {
		sendMessage(MSG.GET_WORKORDER, taskData[id]);
	};

	handlers[MSG.SET_DUTY_CYCLE] = function(value) {
		if (value) {
			dutyCycle = Math.max(0.10, Math.max(.99));
			postDutyCycle();
		}
	};
	handlers[MSG.GET_DUTY_CYCLE] = function() {
		postDutyCycle();
	};

	handlers[MSG.SET_RUNNING] = function(value) {
		switch (value) {
		case true:
			resumeTask();
			break;
		case false:
			pauseTask();
			break;
		default:
			return;
		}
	};
	handlers[MSG.IS_RUNNING] = function() {
		postIsRunning();
	};

	handlers[MSG.CANCEL] = function() {
		cancelTask();
	};

	function handleMessage(msg) {
		if (msg) {
			var f = handlers[msg.name];
			if (f) {
				f(msg.value);
			}
		}
	}

	function defaultSetup(data) {
		return data;
	}

	function postDutyCycle() {
		sendMessage(MSG.GET_DUTY_CYCLE, dutyCycle);
	}
	function postIsRunning() {
		sendMessage(MSG.IS_RUNNING, executionTimer ? true : false);
	}
	function postStatus(status) {
		if (status instanceof String) {
			sendMessage(MSG.SET_STATUS, status);
		}
	}

	function cancelTimer() {
		if (executionTimer) {
			var t = executionTimer;
			executionTimer = null;
			clearTimer(t);
		}
	}

	function resumeTask() {
		if (executionTimer) {
			return; // We are already running so do nothing
		}

		if (isInitialized) {
			executionTimer = _setTimeout(loop, 1);
		} else {
			executionTimer = _setTimeout(setup, 1);
		}

		postIsRunning();
	}
	function pauseTask() {
		if (!executionTimer) {
			return; // We are already paused so do nothing
		}

		cancelTimer();
		postIsRunning();
	}
	function cancelTask() {
		cancelTimer();
		postIsRunning();
	}

	function setup() {
		if ($.setup) {
			try {
				taskState = new Object();
				$.setup.call(taskState, taskActive.data);
				isInitialized = true;
			} catch (err) {
				postError(err);
				return;
			}
		} else {
			taskState = taskActive.data;
		}
		executionTimer = _setTimeout(loop, 1);
		isInitialized = true;
	}

	function loop() {
		try {
			var r = $.iterate.call(taskState);
			if (r) {
				var r = $.finish.call(taskState);
				sendMessage(MSG.FINISHED, {
					id : taskActive.id,
					task : taskActive.task.id,
					data : r
				});
				taskActive = null;
				taskState = null;
				isInitialize = false;
				executionTimer = null;
			} else {
				// TODO: Estimate duty cycle
				executionTimer = _setTimeout(loop, 1000);
			}
		} catch (err) {
			postError(err);
		}
	}

	// Change the jobs status text in the display
	function postError(err) {
		sendMessage(MSG.ERROR, {
			name : err.name,
			type : err.type,
			message : err.message,
			stack : err.stack
		});
	}
	function postLog(msg) {
	    sendMessage(MSG.DEBUG, JSON.stringify(msg));
	}
	function sendMessage(name, value) {
		_postMessage(createMessage(name, value));
	}

	return function(evt) {
		try {
			dispatchMessages(evt.data, handleMessage);
		} catch (err) {
			postError(err);
		}
	};
})();
