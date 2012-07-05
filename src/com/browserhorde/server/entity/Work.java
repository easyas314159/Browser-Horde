package com.browserhorde.server.entity;

@MappedSuperclass
public class Work extends BaseObject {
	@Expose
	private String name;

	@Expose
	private String description;

	@Expose
	private Boolean active;

	@Expose
	private Boolean boost;

	@Expose
	private Integer timeout;

	@Expose
	@ManyToOne
	private Script script;

	@Expose
	private String callback;

	private String randomizer;
}
