(function() {

var t = {now: 0.0};

$.setup = function(data) {
	timer(t);
	timer(t);

	this.idx = 0;
	this.result = [];
};
$.iterate = function() {
	if(this.idx < modules.length) {
		$.status("Module " + (this.idx + 1));
		modules[this.idx].call(this);
		this.idx++;
	}
	else {
		return this.result;
	}
};

var A0 = 1.0;
var A1 = -0.1666666666671334;
var A2 = 0.833333333809067E-2;
var A3 = 0.198412715551283E-3;
var A4 = 0.27557589750762E-5;
var A5 = 0.2507059876207E-7;
var A6 = 0.164105986683E-9;

var B0 = 1.0;
var B1 = -0.4999999999982;
var B2 = 0.4166666664651E-1;
var B3 = -0.1388888805755E-2;
var B4 = 0.24801428034E-4;
var B5 = -0.2754213324E-6;
var B6 = 0.20189405E-8;

var C0 = 1.0;
var C1 = 0.99999999668;
var C2 = 0.49999995173;
var C3 = 0.16666704243;
var C4 = 0.4166685027E-1;
var C5 = 0.832672635E-2;
var C6 = 0.140836136E-2;
var C7 = 0.17358267E-3;
var C8 = 0.3931683E-4;

var D1 = 0.3999999946405E-1, D2 = 0.96E-3, D3 = 0.1233153E-5;
var E2 = 0.48E-3, E3 = 0.411051E-6;

var m, n, s, u, v, w, x, t = {};
var loops = 15625, TLimit = 15.0, NLimit = 256000000;

var one = 1.0, two = 2.0, three = 3.0, four = 4.0, five = 5.0;
var piref = 3.14159265358979324;

var modules = [
	/** **************************************************** */
	/* Module 1. Calculate integral of df(x)/f(x) defined */
	/* below. Result is ln(f(1)). There are 14 */
	/* double precision operations per loop */
	/* ( 7 +, 0 -, 6 *, 1 / ) that are included */
	/* in the timing. */
	/* 50.0% +, 00.0% -, 42.9% *, and 07.1% / */
	/** **************************************************** */
	function() {
		var i, n = loops, sa = 0.0;
		while (sa < TLimit) {
			n = 2 * n;
			x = one / n;

			s = 0.0;
			v = 0.0;
			w = one;
			
			timer(t);
			for(i = 0; i < n; i++) {
				v = v + w;
				u = v * x;
				s = s + (D1 + u * (D2 + u * D3))
						/ (w + u * (D1 + u * (E2 + u * E3)));
			}
			timer(t);
			sa = t.elapsed;

			if(n == NLimit) {
				break;
			}
		}

		timer(t);
		for(i = 0; i < n; i++) {
		}
		timer(t);

		this.iters = n;
		this.nulltime = Math.max(t.elapsed, 0.0);

		this.result.push({
			add: 7,
			sub: 0,
			mul: 6,
			div: 1,
			t: sa - this.nulltime
		});
	},
	function() {},
	function() {},
	function() {},
	function() {},
	function() {},
	function() {},
	function() {}
];

function timer(p) {
	var q = p.now;
	p.now = 1.0E-3 * (new Date).getTime();
	p.elapsed = p.now - q;
}

})();