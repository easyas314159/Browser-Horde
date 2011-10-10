function requestTaskData(id, callback) {
    $.ajax({
	    type: 'GET',
	    dataType: 'json',
	    url: apiPath + "/tasks/" + id + "/data",
	    success: callback,
	    crossDomain: true
    });
}
function requestWorkorder(callback) {
	$.ajax({
			type: 'GET',
			dataType: 'json',
			url: apiPath + "/workorders",
			statusCode: statusCodeHandlers,
			success: callback,
		});
}
function submitWorkorder(data, callback) {
	$.ajax({
			type: 'POST',
			contentType: 'application/json',
			dataType: 'json',
			data: JSON.stringify(data),
			url: apiPath + "/workorders",
			success: callback
		});
}