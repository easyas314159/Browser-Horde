var dutyCycle = .90;
var executionTimer = null;
var isInitialized = false;

var taskScript = {};
var taskState, taskActive;

var handlers = {};
importScripts('messaging.js');

handlers[MSG.PRELOAD_SCRIPT] = function(id) {
		$ = taskScript[id];
		if(!$) {
			// Load the script if we haven't already
			$ = {};

			// Simple test to prevent a malicious script from overwriting onmessage
			var onmessageStored = onmessage;
			importScripts("scripts/" + id + ".js");
			onmessage = onmessageStored;

			if(!($.setup && $.step)) {
				sendMessage(MSG.PRELOAD_FAILED, null);
			}
			else {
				taskScript[id] = $;
			}
		}
		sendMessage(MSG.PRELOAD_COMPLETE, id);
	};
handlers[MSG.SET_WORKORDER] = function(wo) {
		if(wo && wo.id) {
			if(taskActive) {
				if(taskActive.id && wo.id == taskActive.id) {
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
		if(value) {
			dutyCycle = Math.max(0.10, Math.max(.99));
			postDutyCycle();
		}
	};
handlers[MSG.GET_DUTY_CYCLE] = function() {
		postDutyCycle();
	};

handlers[MSG.SET_RUNNING] = function(value) {
		switch(value) {
		case true:
			resumeTask();
			break;
		case false:
			pauseTask();
			break;
		default: return;
		}
	};
handlers[MSG.IS_RUNNING] = function() {
		postIsRunning();
	};

//	(t / d) - t;
handlers[MSG.CANCEL] = function() {
		cancelTask();
	};

function postDutyCycle() {
	sendMessage(MSG.GET_DUTY_CYCLE, dutyCycle);
}
function postIsRunning() {
	sendMessage(MSG.IS_RUNNING, executionTimer ? true : false);
}
function postStatus(status) {
	if(status instanceof String) {
		sendMessage(MSG.SET_STATUS, status);
	}
}

function cancelTimer() {
	if(executionTimer) {
		var t = executionTimer;
		executionTimer = null;
		clearTimer(t);
	}
}

function resumeTask() {
	if(executionTimer) {
		return; // We are already running so do nothing
	}

	if(isInitialized) {
		executionTimer = setTimeout("loop()", 1);
	}
	else {
		executionTimer = setTimeout("setup()", 1);
	}

	postIsRunning();
}
function pauseTask() {
	if(!executionTimer) {
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
	if($.setup) {
		try {
			taskState = {};
			$.setup(taskState, taskActive.data);
			isInitialized = true;
		}
		catch(err) {
			postError(err);
			return
		}
	}
	else {
		taskState = taskActive.data;
	}
	executionTimer = setTimeout("loop()", 1);
	isInitialized = true;
}

function loop() {
	try {
		var r = $.step(taskState);
		if(r == null) {
			// TODO: Estimate duty cycle
			executionTimer = setTimeout("step()", 1000);
		}
		else {
			sendMessage(MSG.FINISHED, {
					id: taskActive.id,
					task: taskActive.task.id,
					data: r
				});
			taskActive = null;
			taskState = null;
			isInitialize = false;
			executionTimer = null;
		}
	}
	catch(err) {
		postError(err);
	}
}

// Change the jobs status text in the display
function setStatus(status) {
	postMessage({
		status: status
	});
}

function postError(err) {
	sendMessage(MSG.ERROR, {
			name: err.name,
			type: err.type,
			message: err.message,
			stack: err.stack
		});
}
function sendMessage(name, value) {
	postMessage(createMessage(name, value));
}

console = {
	log: function(msg) {post("console.log", msg);}
};

onmessage = function(evt) {
	try {
		dispatchMessages(evt.data, handleMessage);
	}
	catch(err) {
		postError(err);
	}
};

function handleMessage(msg) {
	if(msg) {
		var f = handlers[msg.name];
		if(f) {f(msg.value);}
	}
}
