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

	this.r = new Object();
	this.r.r = new Array();

	this.r.b = {
			x: data.na, w: data.la,
			y: data.nb, h: data.lb
		};
};

$.iterate = function() {
	var a0, b0, an, bn, done;

	if(this.x == this.r.b.w) {
		this.x = 0;
		this.y++;
	}
	if(this.y == this.r.b.h) {
		return this.r;
	}

	a0 = this.dx * (this.x + this.r.b.x) / this.r.b.w + this.min.x;
	b0 = this.dy * (this.y + this.r.b.y) / this.r.b.h + this.min.y;

	done = this.iters;
	for(var n = 0; n < this.iters; ++n) {
		an = 2.0 * a0 * b0;
		bn = b0*b0 - a0*a0;

		if(an * an + bn * bn > 4.0) {
			done = n;
			break;
		}
	}
	this.r.r.push(done);
	this.x++;
};

})();