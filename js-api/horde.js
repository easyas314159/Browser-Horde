(function(window) {
	
	var Horde = (function() {
		var Horde = function() {
		};

		Horde.fn = Horde.prototype = {
			constructor: Horde,

			 
		};

		return Horde;
	})();

	window.Horde = Horde;
})(window);

/*
Actions
-Check out new task
-Pause task
-Cancel task
-Up/Down vote task

Events
task_started
task_finished
task_error
*/
