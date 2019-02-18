package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriveBasicReadQueries {
	private static Logger logger = LoggerFactory.getLogger(DriveBasicReadQueries.class);

	protected Drive service;

	public DriveBasicReadQueries(Drive service) {
		this.service = service;
	}

	public List<File> listFoldersInRoot() throws IOException {
		logger.debug("listFoldersInRoot ENTER");
		FileList result = service.files().list()
				.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		List<File> folders = result.getFiles();
		logger.debug(String.format("listFoldersInRoot EXIT: %s", folders.size()));
		return folders;
	}

	public Map<File, List<File>> listChildItemsOfFolder(String searchParentFolderName) throws IOException {
		logger.debug("listChildItemsOfFolder ENTER");
		Map<File, List<File>> results = new HashMap<File, List<File>>();
		
		FileList result = service.files().list()
				.setQ(String.format(
						"name = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
						searchParentFolderName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		
		List<File> foldersMatchingSearchName = result.getFiles();
		
		if (foldersMatchingSearchName != null && !foldersMatchingSearchName.isEmpty()) {
			logger.debug(String.format("foldersMatchingSearchName: %s", foldersMatchingSearchName.size()));

			for (File folder : foldersMatchingSearchName) {
				logger.debug(String.format("Found parent folder. name: %s id: %s", folder.getName(), folder.getId()));
				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and trashed = false", folder.getId())).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents)").execute();
				
				List<File> childItems = childResult.getFiles();
				
				if (childItems != null && !childItems.isEmpty()) {
					logger.debug(String.format("childItems: %s", childItems.size()));
					results.put(folder, childItems);
				}
			}
		} else {
			logger.debug("No results.");
		}
		
		return results;
	}
}
