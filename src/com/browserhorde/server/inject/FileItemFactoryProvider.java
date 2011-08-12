package com.browserhorde.server.inject;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import com.google.inject.Provider;

public class FileItemFactoryProvider implements Provider<FileItemFactory> {
	@Override
	public FileItemFactory get() {
		return new DiskFileItemFactory();
	}
}
