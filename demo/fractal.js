(function(){

/*
{
	n: iterations
	p: [{na, da, nb, db}, ...]
}
*/

$.setup = function(data) {
	this.iters = data.n;

	this.x = this.y = 0;
	this.w = data.da;
	this.h = data.db;

	this.min = {x: data.minx, y: data.miny};
	this.max = {x: data.maxx, y: data.maxy};

	this.dx = this.max.x - this.min.x;
	this.dy = this.max.y - this.min.y;

	this.r = {
		r: new Array(),
		b: {
			x: data.na,
			y: data.nb,

			w: data.la,
			h: data.lb
		}
	};
};

$.iterate = function() {
	var a0, b0;

	if(this.x == this.r.b.w) {
		this.x = 0;
		this.y++;
	}
	if(this.y == this.r.b.h) {
		return this.r;
	}

	a0 = this.dx * (this.r.b.x + this.x) / this.r.b.w + this.min.x;
	b0 = this.dy * (this.r.b.y + this.y) / this.r.b.h + this.min.y;

	$.status("[" + a0 + " " + b0 + "]");

	this.r.r.push(test_point({r:0.0, i:0.0}, {r:a0, i:b0}, this.iters));
	this.x++;
};

function test_point(z, c, iters) {
	var done = iters;

	if(!(z && z.r && z.i)) {
		z = {r:0.0, i:0.0};
	}
	if(!(c && c.r && c.i)) {
		c = {r:0.0, i:0.0};
	}

	var zr0 = z.r, zi0 = z.i;
	for(var n = 0; n < iters; ++n) {
		var zr1 = 2.0 * zr0 * zi0 + c.r;
		var zi1 = zi0*zi0 - zr0*zr0 + c.i;

		if(zr1*zr1 + zi1*zi1 > 4.0) {
			done = n;
			break;
		}
		
		zr0 = zr1; zi0 = zi1;
	}

	return done;
}

})();