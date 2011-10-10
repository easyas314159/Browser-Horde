var w, wo;
var statusCodeHandlers = {
    503: function(jqXHR, textStatus, errorThrown) {
	var retryAfter = jqXHR.getResponseHeader("Retry-After");
	if(retryAfter) {
	    // TODO: Parse Retry-After and then wait to retry
	}
	else {
	    console.error(textStatus);
	}
    },
};

function main() {
    if(!window.Worker) {
	$("#upgrade").show();
	return;
    }
    $("#congrats").show();
    checkoutWorkorder();
}

function handleMessage(msg) {
    if(msg) {
	var f = handlers[msg.name];
	if(f) {f(msg.value);}
    }
}

var handlers = {};
handlers[MSG.SET_STATUS] = function(status) {
    setStatus(status);
};
handlers[MSG.IS_RUNNING] = function(running) {
    console.log(running);
};
handlers[MSG.FINISHED] = function(result) {
    checkinWorkorder(result);
};
handlers[MSG.DEBUG] = function(value) {
    value = JSON.parse(value);
    console.log(value);
};
handlers[MSG.ERROR] = function(err) {
    console.error(err);
};
 
function handleError(evt) {
	console.error(evt);
}

function resumeProcessing() {
	sendMessage(MSG.SET_RUNNING, true);
}
function pauseProcessing() {
	sendMessage(MSG.SET_RUNNING, false);
}
function cancelProcessing() {
	sendMessage(MSG.CANCEL);
}

function checkoutWorkorder() {
	requestWorkorder(executeWorkorder);
}
function checkinWorkorder(data) {
	submitWorkorder(data, checkoutWorkorder);
}
function setStatus(status) {
    $(".job_status").text(status);
}

function executeWorkorder(data, textStatus, jqXHR) {
	if(data == null) {
		$("#job").fadeOut();
		$("#nowork").fadeIn();

		setTimeout(checkoutWorkorder, 60000);
	}
	else {
		var task = data.task;
		var job = task.job;
		var script = job.script;

		$(".job_name").text(job.name);
		setStatus("Retrieving Task Data");
		$(".job_description").text(job.name);
		$(".job_website").text(job.website);
		$(".job_website").attr("href", job.website);

		$("#job").fadeIn();
		$("#nowork").fadeOut();

		if(w) {
			w.terminate();
			w = null;
		}

		w = new Worker("work.js");
		w.onmessage = function(evt) {
				dispatchMessages(evt.data, handleMessage);
			};
		w.onerror = handleError;

		sendMessage(MSG.PRELOAD_SCRIPT, script.id);

		wo = data;
		requestTaskData(task.id, receiveTaskData);
	}
}
function receiveTaskData(data, textStatus, jqXHR) {
	wo.data = data;
	sendMessage(MSG.SET_WORKORDER, wo);
	sendMessage(MSG.SET_RUNNING, true);
}

function pushMessage(name, value) {
}
function flushMessages() {
}
function sendMessage(name, value) {
	flushMessages();
	w.postMessage(createMessage(name, value));
}