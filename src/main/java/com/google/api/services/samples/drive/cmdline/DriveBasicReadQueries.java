package com.google.api.services.samples.drive.cmdline;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class DriveBasicReadQueries {
	private static Logger logger = LoggerFactory.getLogger(DriveBasicReadQueries.class);

	protected Drive service;

	public DriveBasicReadQueries(Drive service) {
		this.service = service;
	}

	public List<File> listFoldersInRoot() throws IOException {
		logger.info("~~~~~~~~~ listFoldersInRoot ~~~~~~~~~~~~");

		FileList result = service.files().list()
				.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		List<File> folders = result.getFiles();
		
		return folders;
	}

	public void listChildItemsOfFolder(String searchParentFolderName) throws IOException {
		logger.info("~~~~~~~~~ listChildItemsOfFolder ~~~~~~~~~~~~");

		FileList result = service.files().list()
				.setQ(String.format(
						"name = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
						searchParentFolderName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		List<File> folders = result.getFiles();
		if (folders != null && !folders.isEmpty()) {
			for (File folder : folders) {
				logger.info(String.format("Found parent folder. name: %s id: %s", folder.getName(), folder.getId()));

				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and trashed = false", folder.getId())).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents)").execute();
				List<File> childItems = childResult.getFiles();
				if (childItems != null && !childItems.isEmpty()) {
					for (File childItem : childItems) {
						logger.info(String.format("Iterating child item. name: %s id: %s", childItem.getName(),
								childItem.getId()));
					}
				}
			}
		}
	}
}
