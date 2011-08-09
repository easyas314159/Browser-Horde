package com.browserhorde.server.jpa;

import java.io.Serializable;

import javax.persistence.Entity;

@Entity
public class Job implements Serializable {
	private String name;
	private String description;
}
