package com.browserhorde.server.entity;

import java.util.Date;

public interface Timestamped {
	public Date getCreated();
	public void setCreated(Date created);

	public Date getUpdated();
	public void setUpdated(Date updated);
}
