package com.browserhorde.server.entity;

@Entity
public class Project extends Work {
	@Expose
	@ManyToOne
	private User owner;

	@Expose
	private String website;
}
