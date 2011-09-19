// Derived from http://home.iae.nl/users/mhx/flops.c

var T = {};

var sa, sb, sc, sd, one, two, three;
var four, five, piref, piprg;
var scale, pierr;

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

var D1 = 0.3999999946405E-1;
var D2 = 0.96E-3;
var D3 = 0.1233153E-5;

var E2 = 0.48E-3;
var E3 = 0.411051E-6;

var t = {
	now : 0.0,
	elapsed : 0.0
};

onmessage = function(event) {
	postMessage(estimate_flops());
};

function estimate_flops() {
	var space = {};

	var i, m, n;
	var s, u, v, w, x;
	var loops = 15625;

	var TLimit = 15.0;
	//var TLimit = 3.0;
	var NLimit = 512000000;

	var piref = 3.14159265358979324;
	var one = 1.0;
	var two = 2.0;
	var three = 3.0;
	var four = 4.0;
	var five = 5.0;
	var scale = one;

	dtime(t);
	dtime(t);

	/** **************************************************** */
	/* Module 1. Calculate integral of df(x)/f(x) defined */
	/* below. Result is ln(f(1)). There are 14 */
	/* double precision operations per loop */
	/* ( 7 +, 0 -, 6 *, 1 / ) that are included */
	/* in the timing. */
	/* 50.0% +, 00.0% -, 42.9% *, and 07.1% / */
	/** **************************************************** */

	var n = loops;
	var sa = 0.0;

	while (sa < TLimit) {
		n = 2 * n;
		x = one / n;

		s = 0.0;
		v = 0.0;
		w = one;

		dtime(t);
		for (i = 0; i < n; i++) {
			v = v + w;
			u = v * x;
			s = s + (D1 + u * (D2 + u * D3))
					/ (w + u * (D1 + u * (E2 + u * E3)));
		}
		dtime(t);
		sa = t.elapsed;

		if(n == NLimit)
			break;
	}

	scale = 1.0E+06 / n;

	/** ************************************* */
	/* Estimate nulltime ('for' loop time). */
	/** ************************************* */

	dtime(t);
	for (i = 0; i < n; i++) {
	}
	dtime(t);

	nulltime = Math.max(t.elapsed, 0.0);
	T[2] = scale * (sa - nulltime);

	sa = (D1+D2+D3)/(one+D1+E2+E3);
	sb = D1;

	sa = x * ( sa + sb + two * s ) / two;
	sb = one / sa;
	sc = sb - 25.2;

	n  = Math.floor(40000 * Math.floor(sb) / scale);

	space[1] = {
		FADD: 7,
		FSUB: 0,
		FMUL: 6,
		FDIV: 1,
		error: sc,
		elapsed: T[2]
	};

	m = n;

	/** **************************************************** */
	/* Module 2. Calculate value of PI from Taylor Series */
	/* expansion of atan(1.0). There are 7 */
	/* double precision operations per loop */
	/* ( 3 +, 2 -, 1 *, 1 / ) that are included */
	/* in the timing. */
	/* 42.9% +, 28.6% -, 14.3% *, and 14.3% / */
	/** **************************************************** */

	s  = -five;
	sa = -one;

	dtime(t);
	for(i = 0; i < m; i++ ) {
		s  = -s;
		sa = sa + s;
	}
	dtime(t);
	sc = t.elapsed;

	u = sa;
	v = 0.0;
	w = 0.0;
	x = 0.0;

	dtime(t);
	for(i = 0; i < m; i++) {
		s  = -s;
		sa = sa + s;
		u  = u + two;
		x  = x +(s - u);
		v  = v - s * u;
		w  = w + s / u;
	}
	dtime(t);

	T[6] = scale * (t.elapsed - sc);

	sa = four * w / five;
	sb = sa + five / v;
	sc = 31.25;
	piprg = sb - sc / (v * v * v);
	pierr = piprg - piref;

	space[2] = {
		module: 2,
		FADD: 3,
		FSUB: 2,
		FMUL: 1,
		FDIV: 1,
		error: pierr,
		elapsed: T[6]
	};

	/** **************************************************** */
	/* Module 3. Calculate integral of sin(x) from 0.0 to */
	/* PI/3.0 using Trapazoidal Method. Result */
	/* is 0.5. There are 17 double precision */
	/* operations per loop (6 +, 2 -, 9 *, 0 /) */
	/* included in the timing. */
	/* 35.3% +, 11.8% -, 52.9% *, and 00.0% / */
	/** **************************************************** */

	x = piref / (three * m);
	s = 0.0;
	v = 0.0;

	dtime(t);
	for( i = 0 ; i < m; i++) {
		v = v + one;
		u = v * x;
		w = u * u;
		s = s + u * ((((((A6*w-A5)*w+A4)*w-A3)*w+A2)*w+A1)*w+one);
	}
	dtime(t);

	T[9]  = scale * (t.elapsed - nulltime);

	u  = piref / three;
	w  = u * u;
	sa = u * ((((((A6*w-A5)*w+A4)*w-A3)*w+A2)*w+A1)*w+one);

	sa = x * ( sa + two * s ) / two;
	sb = 0.5;
	sc = sa - sb;

	space[3] = {
		FADD: 6,
		FSUB: 2,
		FMUL: 9,
		FDIV: 0,
		error: sc,
		elapsed: T[9]
	};

	/** ********************************************************* */
	/* Module 4. Calculate Integral of cos(x) from 0.0 to PI/3 */
	/* using the Trapazoidal Method. Result is */
	/* sin(PI/3). There are 15 double precision */
	/* operations per loop (7 +, 0 -, 8 *, and 0 / ) */
	/* included in the timing. */
	/* 50.0% +, 00.0% -, 50.0% *, 00.0% / */
	/** ********************************************************* */
	A3 = -A3;
	A5 = -A5;
	x = piref / ( three * m );
	s = 0.0;
	v = 0.0;
	
	dtime(t);
	for(i = 0 ; i < m; i++) {
		u = i * x;
		w = u * u;
		s = s + w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
	}
	dtime(t);
	T[12]  = scale * (t.elapsed - nulltime);

	u  = piref / three;
	w  = u * u;
	sa = w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
	sa = x * ( sa + one + two * s ) / two;
	u  = piref / three;
	w  = u * u;
	sb = u * ((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+A0);
	sc = sa - sb;

	space[4] = {
		FADD: 7,
		FSUB: 0,
		FMUL: 8,
		FDIV: 0,
		error: sc,
		elapsed: T[12]
	};

	/** ********************************************************* */
	/* Module 5. Calculate Integral of tan(x) from 0.0 to PI/3 */
	/* using the Trapazoidal Method. Result is */
	/* ln(cos(PI/3)). There are 29 double precision */
	/* operations per loop (13 +, 0 -, 15 *, and 1 /) */
	/* included in the timing. */
	/* 46.7% +, 00.0% -, 50.0% *, and 03.3% / */
	/** ********************************************************* */
	
	x = piref / ( three * m );
	s = 0.0;
	v = 0.0;

	dtime(t);
	for( i = 0 ; i < m; i++ ) {
		u = i * x;
		w = u * u;
		v = u * ((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
		s = s + v / (w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one);
	}
	dtime(t);
	T[15]  = scale * (t.elapsed - nulltime);

	u  = piref / three;
	w  = u * u;
	sa = u*((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
	sb = w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
	sa = sa / sb;
	
	sa = x * ( sa + two * s ) / two;
	sb = 0.6931471805599453;
	sc = sa - sb;
	
	space[5] = {
		FADD: 13,
		FSUB: 0,
		FMUL: 15,
		FDIV: 1,
		error: sc,
		elapsed: T[15]
	};

	/************************************************************/
	/* Module 6.  Calculate Integral of sin(x)*cos(x) from 0.0  */
	/*            to PI/4 using the Trapazoidal Method. Result  */
	/*            is sin(PI/4)^2. There are 29 double precision */
	/*            operations per loop (13 +, 0 -, 16 *, and 0 /)*/
	/*            included in the timing.                       */
	/*            46.7% +, 00.0% -, 53.3% *, and 00.0% /        */
	/************************************************************/

	x = piref / ( four * m );
	s = 0.0;
	v = 0.0;

	dtime(t);
	for( i = 0; i < m-1; i++ ) {
		u = i * x;
		w = u * u;
		v = u * ((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
		s = s + v*(w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one);
	}
	dtime(t);
	T[18]  = scale * (t.elapsed - nulltime);

	u  = piref / four;
	w  = u * u;
	sa = u*((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
	sb = w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
	sa = sa * sb;

	sa = x * ( sa + two * s ) / two;
	sb = 0.25;
	sc = sa - sb;

	space[6] = {
		FADD: 13,
		FSUB: 0,
		FMUL: 16,
		FDIV: 0,
		error: sc,
		elapsed: T[18]
	};

   /*******************************************************/
   /* Module 7.  Calculate value of the definite integral */
   /*            from 0 to sa of 1/(x+1), x/(x*x+1), and  */
   /*            x*x/(x*x*x+1) using the Trapizoidal Rule.*/
   /*            There are 12 double precision operations */
   /*            per loop ( 3 +, 3 -, 3 *, and 3 / ) that */
   /*            are included in the timing.              */
   /*            25.0% +, 25.0% -, 25.0% *, and 25.0% /   */
   /*******************************************************/

	s = 0.0;
	w = one;
	sa = 102.3321513995275;
	v = sa / m;

	dtime(t);
	for (i = 0; i < m-1; i++) {
		x = i * v;
		u = x * x;
		s = s - w / ( x + w ) - x / ( u + w ) - u / ( x * u + w );
	}
	dtime(t);
	T[21] = scale * (t.elapsed - nulltime);

	x  = sa;                                      
	u  = x * x;
	sa = -w - w / ( x + w ) - x / ( u + w ) - u / ( x * u + w );
	sa = 18.0 * v * (sa + two * s );

	m  = -2000 * Math.floor(sa);
	m = Math.floor(m / scale);

	sc = sa + 500.2;

	space[7] = {
		FADD: 3,
		FSUB: 3,
		FMUL: 3,
		FDIV: 3,
		error: sc,
		elapsed: T[21]
	};

   /************************************************************/
   /* Module 8.  Calculate Integral of sin(x)*cos(x)*cos(x)    */
   /*            from 0 to PI/3 using the Trapazoidal Method.  */
   /*            Result is (1-cos(PI/3)^3)/3. There are 30     */
   /*            double precision operations per loop included */
   /*            in the timing:                                */
   /*               13 +,     0 -,    17 *          0 /        */
   /*            46.7% +, 00.0% -, 53.3% *, and 00.0% /        */
   /************************************************************/

	x = piref / ( three * m );
	s = 0.0;
	v = 0.0;

	dtime(t);
	for( i = 1 ; i <= m-1 ; i++ ) {
		u = i * x;
		w = u * u;
		v = w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
		s = s + v*v*u*((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
	}
	dtime(t);
	T[24]  = scale * (t.elapsed - nulltime);

	u  = piref / three;
	w  = u * u;
	sa = u*((((((A6*w+A5)*w+A4)*w+A3)*w+A2)*w+A1)*w+one);
	sb = w*(w*(w*(w*(w*(B6*w+B5)+B4)+B3)+B2)+B1)+one;
	sa = sa * sb * sb;

	sa = x * ( sa + two * s ) / two;
	sb = 0.29166666666666667;
	sc = sa - sb;

	space[8] = {
		FADD: 13,
		FSUB: 0,
		FMUL: 17,
		FDIV: 0,
		error: sc,
		elapsed: T[24]
	};

  /**************************************************/   
  /* MFLOPS(1) output. This is the same weighting   */
  /* used for all previous versions of the flops.c  */
  /* program. Includes Modules 2 and 3 only.        */
  /**************************************************/ 
     T[27] = ( five * T[6] + T[9] ) / 52.0;
     T[28] = one  / T[27];

  /**************************************************/   
  /* MFLOPS(2) output. This output does not include */
  /* Module 2, but it still does 9.2% FDIV's.       */
  /**************************************************/ 
     T[29] = T[2] + T[9] + T[12] + T[15] + T[18];
     T[29] = (T[29] + four * T[21]) / 152.0;
     T[30] = one / T[29];

  /**************************************************/   
  /* MFLOPS(3) output. This output does not include */
  /* Module 2, but it still does 3.4% FDIV's.       */
  /**************************************************/ 
     T[31] = T[2] + T[9] + T[12] + T[15] + T[18];
     T[31] = (T[31] + T[21] + T[24]) / 146.0;
     T[32] = one / T[31];

  /**************************************************/   
  /* MFLOPS(4) output. This output does not include */
  /* Module 2, and it does NO FDIV's.               */
  /**************************************************/ 
 T[33] = (T[9] + T[12] + T[18] + T[24]) / 91.0;
 T[34] = one / T[33];	                  

	return {
		modules: space,
		iterations: m,
		"MFLOPS(1)" : T[28],
		"MFLOPS(2)" : T[30],
		"MFLOPS(3)" : T[32],
		"MFLOPS(4)" : T[34]
	};
}

var modules = {};
modules[1] = function() {
	
}

function dtime(p) {
	var q = p.now;

	p.now = (new Date).getTime() * 1.0E-3;
	p.elapsed = p.now - q;
}