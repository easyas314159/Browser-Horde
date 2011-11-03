package com.browserhorde.server.util;

import net.spy.memcached.CASMutation;
import net.spy.memcached.CASResponse;
import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.compat.SpyObject;
import net.spy.memcached.transcoders.Transcoder;

/**
 * Object that provides mutation via CAS over a given memcache client.
 *
 * <p>
 * Example usage (reinventing incr):
 * </p>
 *
 * <pre>
 * // Get or create a client.
 * MemcachedClient client=[...];
 *
 * // Get a Transcoder.
 * Transcoder<Long> tc = new LongTranscoder();
 *
 * // Get a mutator instance that uses that client.
 * CASMutator&lt;Long&gt; mutator=new CASMutator&lt;Long&gt;(client, tc);
 *
 * // Get a mutation that knows what to do when a value is found.
 * CASMutation&lt;Long&gt; mutation=new CASMutation&lt;Long&gt;() {
 *     public Long getNewValue(Long current) {
 *         return current + 1;
 *     }
 * };
 *
 * // Do a mutation.
 * long currentValue=mutator.cas(someKey, 0L, 0, mutation);
 * </pre>
 */
// TODO: This is a fix for a bug in the official CASMutator<T> object
@Deprecated
public class FixedCASMutator<T> extends SpyObject {
	  private static final int MAX_TRIES = 8192;

	  private final MemcachedClient client;
	  private final Transcoder<T> transcoder;
	  private final int max;

	  /**
	   * Construct a CASMutator that uses the given client.
	   *
	   * @param c the client
	   * @param tc the Transcoder to use
	   * @param max_tries the maximum number of attempts to get a CAS to succeed
	   */
	  public FixedCASMutator(MemcachedClient c, Transcoder<T> tc, int maxTries) {
	    super();
	    client = c;
	    transcoder = tc;
	    max = maxTries;
	  }

	  /**
	   * Construct a CASMutator that uses the given client.
	   *
	   * @param c the client
	   * @param tc the Transcoder to use
	   */
	  public FixedCASMutator(MemcachedClient c, Transcoder<T> tc) {
	    this(c, tc, MAX_TRIES);
	  }

	  /**
	   * CAS a new value in for a key.
	   *
	   * <p>
	   * Note that if initial is null, this method will only update existing values.
	   * </p>
	   *
	   * @param key the key to be CASed
	   * @param initial the value to use when the object is not cached
	   * @param exp the expiration time to use when initializing
	   * @param m the mutation to perform on an object if a value exists for the key
	   * @return the new value that was set
	   */
	  public T cas(final String key, final T initial, int exp,
	      final CASMutation<T> m) throws Exception {
	    T rv = initial;

	    boolean done = false;
	    for (int i = 0; !done && i < max; i++) {
	      CASValue<T> casval = client.gets(key, transcoder);
	      T current = null;
	      // If there were a CAS value, check to see if it's compatible.
	      if (casval != null) {
	        T tmp = casval.getValue();
	        current = tmp;
	      }
	      // If we have anything mutate and CAS, else add.
	      if (current != null) {
	        // Declaring this impossible since the only way current can
	        // be non-null is if casval was set.
	        assert casval != null : "casval was null with a current value";

	        rv = m.getNewValue(current);
	        // There are three possibilities here:
	        // 1) It worked and we're done.
	        // 2) It collided and we need to reload and try again.
	        // 3) It disappeared between our fetch and our cas.
	        // We're ignoring #3 because it's *extremely* unlikely and the
	        // behavior will be fine in this code -- we'll do another gets
	        // and follow it up with either an add or another cas depending
	        // on whether it exists the next time.
	        if (client.cas(key, casval.getCas(), exp, rv, transcoder)
	            == CASResponse.OK) {
	          done = true;
	        }
	      } else {
	        // No value found, try an add.
	        if (initial == null) {
	          done = true;
	          rv = null;
	        } else if (client.add(key, exp, initial, transcoder).get()) {
	          done = true;
	          rv = initial;
	        }
	      }
	    }
	    if (!done) {
	      throw new RuntimeException("Couldn't get a CAS in " + max + " attempts");
	    }

	    return rv;
	  }
}
