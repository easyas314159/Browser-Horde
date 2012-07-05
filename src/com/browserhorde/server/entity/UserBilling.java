package com.browserhorde.server.entity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class UserBilling extends BaseObject {
	@ManyToOne
	private User user;

	private boolean active;

	private String token;
}
