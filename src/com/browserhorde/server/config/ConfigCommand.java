package com.browserhorde.server.config;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.log4j.Logger;

public abstract class ConfigCommand implements Command {
	protected final Logger log = Logger.getLogger(getClass());

	@Override
	public boolean execute(Context ctx) throws Exception {
		if(!(ctx instanceof ConfigContext)) {
			return false;
		}
		return execute((ConfigContext)ctx);
	}

	public abstract boolean execute(ConfigContext ctx) throws Exception;
}
