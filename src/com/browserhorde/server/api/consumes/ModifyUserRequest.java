package com.browserhorde.server.api.consumes;

import com.google.gson.annotations.Expose;

public class ModifyUserRequest {
	@Expose public String email;
	@Expose public String password;
}
