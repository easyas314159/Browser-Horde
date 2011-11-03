package com.browserhorde.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;

@Target(value={ElementType.METHOD})
@Retention(value=RetentionPolicy.RUNTIME)
@HttpMethod(value="PATCH")
public @interface PATCH {}
