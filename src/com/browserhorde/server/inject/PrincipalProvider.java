package com.browserhorde.server.inject;

import java.security.Principal;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class PrincipalProvider implements Provider<Principal> {
	@Inject private ServletRequest request;

	@Override
	public Principal get() {
		//return request.getUserPrincipal();
		if(request instanceof HttpServletRequest) {
			return ((HttpServletRequest)request).getUserPrincipal();
		}
		return null;
	}
}
