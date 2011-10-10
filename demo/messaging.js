//var apiPath = "http://localhost:8080/BrowserHorde";
var apiPath = "http://api.browserhorde.net";

MSG = {
		PRELOAD_SCRIPT: "preload_script",
		PRELOAD_COMPLETE: "preload_complete",

		SET_WORKORDER: "set_workorder",
		GET_WORKORDER: "get_workorder",

		SET_DUTY_CYCLE: "set_duty_cycle",
		GET_DUTY_CYCLE: "get_duty_cycle",

		SET_RUNNING: "set_running",
		IS_RUNNING: "is_running",

		DEBUG: "debug",
		CANCEL: "cancel",

		SET_STATUS: "set_status",

		ERROR: "error",
		FINISHED: "finished",
		INTERRUPTED: "interrupted"
	};

function dispatchMessages(msg, callback) {
	if(msg instanceof Array) {
		msg.forEach(callback);
	}
	else {
		callback(msg);
	}
}

function createMessage(name, value) {
	return {
			name: name,
			value: value
		};
}