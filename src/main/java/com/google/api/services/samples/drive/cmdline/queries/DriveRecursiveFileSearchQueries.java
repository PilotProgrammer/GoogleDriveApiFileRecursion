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

	public void test() throws Exception {
//	    Node node = new Node();
//	    node.nodeLevel = 0;
//	    node.itemName = "Be40-notes";

		findAllParentDirectories(service, "Be40-notes"); 
	}

	public static class Node {
//	    public List<Node> childNodes = new ArrayList<Node>(); // TODO, rename this, because it's actually the "parent nodes" - 
//	    // ie, we are doing a reverse tree, starting at the file/item and working our way up through the parents/paths to get to the root node.
		public Integer nodeLevel;
		public File currentItem;
		public Node nextItem;
	}

	private boolean findAllParentDirectories(Drive service, String fileSearch) throws Exception {
		File rootFolder = service.files().get("root").setFields("id, name").execute();

		FileList result = service.files().list().setQ(String.format("name = '%s' and trashed = false", fileSearch))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> searchResults = result.getFiles();
		List<Node> nodeList = new ArrayList<Node>(); 

		if (searchResults != null && !searchResults.isEmpty()) {
			for (File searchResult : searchResults) {
				Node rootNode = new Node();
				rootNode.currentItem = searchResult;
				reverseFileSearch(rootFolder, rootNode);
				nodeList.add(rootNode);
			}
		}

		return true;
	}

	private void reverseFileSearch(File rootFolder, Node node) throws Exception {
		File searchResult = node.currentItem;

		for (String parentFolderId : searchResult.getParents()) {
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}

			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();
			String parentFolderName = parentFolder.getName();
			System.out.println("parentFolderName " + parentFolderName);

			Node nextNode = new Node();
			node.nextItem = nextNode;
			nextNode.currentItem = parentFolder;

			reverseFileSearch(rootFolder, nextNode);

			/*
			 * if (parentFolderName.equals(nextParentNameInQueue)) { boolean found =
			 * recur3(service, queue, actualResults);
			 * 
			 * if (found) { actualResults.add(searchResult); }
			 * 
			 * return found; } else if (rootFolder.getId().equals(parentFolder.getId())) {
			 * actualResults.add(searchResult); oneOfParentsIsRoot = true; }
			 */
		}
	}

	public boolean pathExistsFromFileToRoot(Queue<String> queue, List<File> actualResults) throws IOException {
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
						boolean found = pathExistsFromFileToRoot(queue, actualResults);

						if (found) {
							actualResults.add(searchResult);
						}

						return found;
					} else if (rootFolder.getId().equals(parentFolder.getId())) {
						actualResults.add(searchResult);
						oneOfParentsIsRoot = true;
					}
				}
			}
		}

		return oneOfParentsIsRoot;
	}
}
