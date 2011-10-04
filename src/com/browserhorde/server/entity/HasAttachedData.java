package com.browserhorde.server.entity;

import javax.persistence.Transient;

public interface HasAttachedData {
	@Transient
	public String getAttachmentKey();
}
