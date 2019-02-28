package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriveBasicReadQueries {
	protected Drive service;

	public DriveBasicReadQueries(Drive service) {
		this.service = service;
	}

	public List<File> listFoldersInRoot() throws IOException {
		FileList result = service.files().list()
				.setQ("'root' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		List<File> folders = result.getFiles();

		return folders;
	}

	public Map<File, List<File>> listChildItemsOfFolder(String searchParentFolderName) throws IOException {
		Map<File, List<File>> results = new HashMap<File, List<File>>();
		
		FileList result = service.files().list()
				.setQ(String.format(
						"name = '%s' and mimeType = 'application/vnd.google-apps.folder' and trashed = false",
						searchParentFolderName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();
		
		List<File> foldersMatchingSearchName = result.getFiles();
		
		if (foldersMatchingSearchName != null && !foldersMatchingSearchName.isEmpty()) {
			for (File folder : foldersMatchingSearchName) {
				FileList childResult = service.files().list()
						.setQ(String.format("'%s' in parents and trashed = false", folder.getId())).setSpaces("drive")
						.setFields("nextPageToken, files(id, name, parents)").execute();
				
				List<File> childItems = childResult.getFiles();
				
				if (childItems != null && !childItems.isEmpty()) {
					results.put(folder, childItems);
				}
			}
		}
		
		return results;
	}
}
