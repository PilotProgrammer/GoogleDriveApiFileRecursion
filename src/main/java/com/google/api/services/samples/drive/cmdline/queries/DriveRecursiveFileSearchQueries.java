package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DriveRecursiveFileSearchQueries {
	private static Logger logger = LoggerFactory.getLogger(DriveRecursiveFileSearchQueries.class);

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
	        	logger.info(String.format("nowIteratingFoundSearchResultMatchingFileName.getName: %s getId:%s"
	        		, searchResult.getName(), searchResult.getId()));

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
			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();

			Node nextNode = new Node();
			node.parentItems.add(nextNode);
			nextNode.currentItem = parentFolder;
			nextNode.nodeLevel = node.nodeLevel + 1;
        	logger.info(String.format("addingParentItemAsNextNodeToCurrentNodeParentItems.getName: %s getId: %s nodeLevel: %s"
        			, parentFolder.getName(), parentFolder.getId(), nextNode.nodeLevel));
        	
        	// when we reach the root folder, then we terminate the recursion
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}
			
			reverseFileSearch(rootFolder, nextNode);
		}
	}

	public boolean pathExistsFromFileToRoot(Queue<String> queue, List<File> actualFileResults) throws IOException {
		logger.debug("pathExistsFromFileToRoot ENTER");

		File rootFolder = service.files().get("root").setFields("id, name").execute();

		String searchItemName = queue.poll();
		String nextParentNameInQueue = queue.peek();
		boolean oneOfParentsIsRoot = false;

		FileList result = service.files().list()
				.setQ(String.format("name = '%s' and trashed = false", searchItemName)).setSpaces("drive")
				.setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> searchResults = result.getFiles();
		if (searchResults != null && !searchResults.isEmpty()) {
			for (File searchResult : searchResults) {
				logger.debug(String.format("searchResult.id: %s .name: %s", searchResult.getId(), searchResult.getName()));

				for (String parentFolderId : searchResult.getParents()) {
					File parentFolder = service.files().get(parentFolderId).setFields("id, name").execute();
					String parentFolderName = parentFolder.getName();
					logger.debug(String.format("parentFolder.id: %s .name: %s", parentFolder.getId(), parentFolderName));

					if (parentFolderName.equals(nextParentNameInQueue)) {
						boolean found = pathExistsFromFileToRoot(queue, actualFileResults);

						if (found) {
							actualFileResults.add(searchResult);
						}
						
						logger.debug(String.format("searchResult: %s found: %s", searchResult.getName(), found));
						return found;
					// when we reach the root folder, we terminate the recursion (pathExistsFromFileToRoot is not called again in this "else" block)
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
