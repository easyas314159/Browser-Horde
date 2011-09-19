package com.browserhorde.server;

import java.io.IOException;
import java.net.InetAddress;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.spy.memcached.CASMutation;
import net.spy.memcached.CASMutator;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.transcoders.Transcoder;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.browserhorde.server.api.ApiHeaders;
import com.browserhorde.server.cache.GsonTranscoder;
import com.browserhorde.server.util.GsonUtils;
import com.google.gson.annotations.Expose;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class XRateLimitFilter extends HttpFilter {
	private static final int RATE_LIMIT = 60;
	private static final int RATE_LIMIT_TIMEOUT = 60;
	
	//private static final int RATE_LIMIT = 10;
	//private static final int RATE_LIMIT_TIMEOUT = 60;

	private static final String NS_RATE_LIMIT = DigestUtils.md5Hex("rate_limit");

	private final Logger log = Logger.getLogger(getClass());

	@Inject private MemcachedClient memcached;

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}
	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse rsp, FilterChain chain) throws IOException, ServletException {
		InetAddress ip = InetAddress.getByName(req.getRemoteAddr());
		String key = NS_RATE_LIMIT + DigestUtils.md5Hex(ip.getAddress());

		Transcoder<RateLimit> tc = new GsonTranscoder<RateLimit>(
				GsonUtils.getGsonBuilder(),
				RateLimit.class
			);
		CASMutator<RateLimit> mutator = new CASMutator<RateLimit>(
				memcached, tc
			);

		int now = (int)(System.currentTimeMillis() / 1000);
		int timeout = now + RATE_LIMIT_TIMEOUT;

		RateLimitMutation rateLimitMutation = new RateLimitMutation(RATE_LIMIT, timeout);
		try {
			// FIXME: Something is wrong with the handling of timeouts
			RateLimit rateLimit = memcached.get(key, tc);
			if(rateLimit == null) {
				rateLimit = new RateLimit(RATE_LIMIT, timeout);
				rateLimit = mutator.cas(key, rateLimit, timeout, rateLimitMutation);
			}
			else {
				timeout = rateLimit.getReset();
				rateLimit = mutator.cas(key, rateLimit, timeout, rateLimitMutation);
			}

			rsp.addIntHeader(ApiHeaders.X_RATE_LIMIT, rateLimit.getLimit());
			rsp.addIntHeader(ApiHeaders.X_RATE_LIMIT_REMAINING, rateLimit.getRemaining());
			rsp.addDateHeader(ApiHeaders.X_RATE_LIMIT_RESET, 1000L*rateLimit.getReset());

			if(rateLimit.isExceeded()) {
				rsp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				rsp.addDateHeader("Retry-After", 1000L*rateLimit.getReset());
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
		@Expose private int count = 0;
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
