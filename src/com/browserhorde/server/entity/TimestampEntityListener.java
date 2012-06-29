package com.browserhorde.server.entity;

import java.util.Date;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

public class TimestampEntityListener {
	@PrePersist
	public void prePersist(Object o) {
		if(o instanceof Timestamped) {
			Date now = new Date();
			Timestamped ts = (Timestamped)o;
			ts.setCreated(now);
			ts.setUpdated(now);
		}
	}

	@PreUpdate
	public void preUpdate(Object o) {
		Date now = new Date();
		Timestamped ts = (Timestamped)o;
		ts.setUpdated(now);
	}
}
