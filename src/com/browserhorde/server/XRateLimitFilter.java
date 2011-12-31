package com.browserhorde.server;

import java.io.IOException;
import java.net.InetAddress;

import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.CASMutation;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.api.ApiHeaders;
import com.browserhorde.server.gson.GsonTranscoder;
import com.browserhorde.server.util.FixedCASMutator;
import com.browserhorde.server.util.ParamUtils;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XRateLimitFilter extends HttpFilter {
	private static final String NS_RATE_LIMIT = DigestUtils.md5Hex("rate_limit");

	private final Logger log = Logger.getLogger(getClass());

	private int rateLimit;
	private int rateLimitTimeout;

	private final GsonBuilder gsonBuilder;
	private final MemcachedClient memcached;

	@Inject
	public XRateLimitFilter(GsonBuilder gsonBuilder, @Nullable MemcachedClient memcached) {
		this.gsonBuilder = gsonBuilder;
		this.memcached = memcached;
	}
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		ServletContext context = config.getServletContext();

		rateLimit = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.RATE_LIMIT), 60);
		rateLimitTimeout = ParamUtils.asInteger(context.getInitParameter(ServletInitOptions.RATE_LIMIT_TIMEOUT), 60);
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		if(memcached == null) {
			chain.doFilter(req, rsp);
			return;
		}

		InetAddress ip = InetAddress.getByName(req.getRemoteAddr());
		String key = NS_RATE_LIMIT + DigestUtils.md5Hex(ip.getAddress());

		Transcoder<RateLimit> tc = new GsonTranscoder<RateLimit>(
				gsonBuilder,
				RateLimit.class
			);
		FixedCASMutator<RateLimit> mutator = new FixedCASMutator<RateLimit>(
				memcached, tc
			);

		int now = (int)(System.currentTimeMillis() / 1000);
		int timeout = now + rateLimitTimeout;

		RateLimitMutation rateLimitMutation = new RateLimitMutation(rateLimit, timeout);
		try {
			// FIXME: Something is wrong with the handling of timeouts
			// Timeouts are being reset to 0 after the initial set because of a defect in CASMutator
			RateLimit lim = memcached.get(key, tc);
			if(lim == null) {
				lim = new RateLimit(rateLimit, timeout);
				lim = mutator.cas(key, lim, timeout, rateLimitMutation);
			}
			else {
				timeout = lim.getReset();
				lim = mutator.cas(key, lim, timeout, rateLimitMutation);
			}

			rsp.addIntHeader(ApiHeaders.X_RATE_LIMIT, lim.getLimit());
			rsp.addIntHeader(ApiHeaders.X_RATE_LIMIT_REMAINING, lim.getRemaining());
			rsp.addDateHeader(ApiHeaders.X_RATE_LIMIT_RESET, 1000L*lim.getReset());

			if(lim.isExceeded()) {
				rsp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				rsp.addDateHeader("Retry-After", 1000L*lim.getReset());
			}
			else {
				chain.doFilter(req, rsp);
			}
		}
		catch(Exception ex) {
			rsp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			log.error("Rate Limit CAS Failed!", ex);
		}
	}

	private static final class RateLimit {
		@Expose private int limit = Integer.MAX_VALUE;
		@Expose private int count = 1;
		@Expose private int reset = Integer.MAX_VALUE;

		public RateLimit() {
		}
		public RateLimit(int limit, int reset) {
			this.limit = limit;
			this.reset = reset;
		}

		public boolean isExceeded() {
			return limit < count;
		}

		public int getLimit() {
			return limit;
		}
		public void setLimit(int limit) {
			this.limit = limit;
		}

		public int getCount() {
			return count;
		}
		public void setCount(int count) {
			this.count = count;
		}

		public int getRemaining() {
			return Math.max(getLimit() - getCount(), 0);
		}

		public int getReset() {
			return reset;
		}
		public void setReset(int reset) {
			this.reset = reset;
		}
	}
	private static final class RateLimitMutation implements CASMutation<RateLimit> {
		private final RateLimit newRateLimit = new RateLimit();
		
		private final int limit;
		private final int reset;

		public RateLimitMutation(int limit, int reset) {
			this.limit = limit;
			this.reset = reset;
		}

		@Override
		public RateLimit getNewValue(RateLimit current) {
			newRateLimit.setLimit(Math.min(current.getLimit(), limit));
			newRateLimit.setReset(Math.min(current.getReset(), reset));
			newRateLimit.setCount(current.getCount() + 1);

			return newRateLimit;
		}
	}
}
