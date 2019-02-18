package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DriveRecursiveFileSearchQueries {
	protected Drive service;

	public DriveRecursiveFileSearchQueries(Drive service) {
		this.service = service;
	}

	public static class Node {
		public Integer nodeLevel;
		public File currentItem;
		public List<Node> parentItems = new ArrayList<Node>();
	}

	public List<Node> findAllParentDirectories(String fileNameToSearch) throws IOException {
		List<Node> returnNodeList = new ArrayList<Node>(); 
		File rootFolder = service.files().get("root").setFields("id, name").execute();

		FileList result = service.files().list().setQ(String.format("name = '%s' and trashed = false", fileNameToSearch))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> searchResults = result.getFiles();

		if (searchResults != null && !searchResults.isEmpty()) {
			for (File searchResult : searchResults) {
				Node rootNode = new Node();
				rootNode.currentItem = searchResult;
				rootNode.nodeLevel = 0;
				reverseFileSearch(rootFolder, rootNode);
				returnNodeList.add(rootNode);
			}
		}

		return returnNodeList;
	}

	private void reverseFileSearch(File rootFolder, Node node) throws IOException {
		File searchResult = node.currentItem;

		for (String parentFolderId : searchResult.getParents()) {
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}

			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();
			String parentFolderName = parentFolder.getName();
			System.out.println("parentFolderName " + parentFolderName);

			Node nextNode = new Node();
			node.parentItems.add(nextNode);
			nextNode.currentItem = parentFolder;
			nextNode.nodeLevel = node.nodeLevel + 1;

			reverseFileSearch(rootFolder, nextNode);
		}
	}

	public boolean pathExistsFromFileToRoot(Queue<String> queue, List<File> actualFileResults) throws IOException {
		File rootFolder = service.files().get("root").setFields("id, name").execute();

		String searchFolderName = queue.poll();
		String nextParentNameInQueue = queue.peek();
		boolean oneOfParentsIsRoot = false;

		FileList result = service.files().list()
				.setQ(String.format("name = '%s' and trashed = false", searchFolderName)).setSpaces("drive")
				.setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> searchResults = result.getFiles();
		if (searchResults != null && !searchResults.isEmpty()) {
			for (File searchResult : searchResults) {

				for (String parentFolderId : searchResult.getParents()) {
					File parentFolder = service.files().get(parentFolderId).setFields("id, name").execute();
					String parentFolderName = parentFolder.getName();

					if (parentFolderName.equals(nextParentNameInQueue)) {
						boolean found = pathExistsFromFileToRoot(queue, actualFileResults);

						if (found) {
							actualFileResults.add(searchResult);
						}

						return found;
					} else if (rootFolder.getId().equals(parentFolder.getId())) {
						actualFileResults.add(searchResult);
						oneOfParentsIsRoot = true;
					}
				}
			}
		}

		return oneOfParentsIsRoot;
	}
}
