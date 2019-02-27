package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DriveRecursiveFileSearchQueries {
	private static Logger logger = LoggerFactory.getLogger(DriveRecursiveFileSearchQueries.class);

	protected Drive service;
	protected File rootFolder;
	protected String fileNameToSearch;
	protected List<Node> allPathsFromTargetFileToRoot;

	public DriveRecursiveFileSearchQueries(Drive service, String fileNameToSearch) throws IOException {
		this.service = service;
		rootFolder = service.files().get("root").setFields("id, name").execute();
		this.fileNameToSearch = fileNameToSearch;
	}

	public static class Node {
		public Integer nodeLevel;
		public File currentItem;
		public List<Node> parentItems = new ArrayList<Node>();
	}

	public List<Node> findAllPathsFromTargetFileToRoot() throws IOException {
		if (allPathsFromTargetFileToRoot != null) {
			return allPathsFromTargetFileToRoot;
		}
		
		allPathsFromTargetFileToRoot = new ArrayList<Node>();

		FileList result = service.files().list()
				.setQ(String.format("name = '%s' and trashed = false", fileNameToSearch)).setSpaces("drive")
				.setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> searchResults = result.getFiles();

		if (searchResults != null && !searchResults.isEmpty()) {
			for (File searchResult : searchResults) {
				logger.info(String.format("nowIteratingFoundSearchResultMatchingFileName.getName: %s getId:%s",
						searchResult.getName(), searchResult.getId()));

				Node rootNode = new Node();
				rootNode.currentItem = searchResult;
				rootNode.nodeLevel = 0;
				reverseFileSearch(rootFolder, rootNode);
				allPathsFromTargetFileToRoot.add(rootNode);
			}
		}
		
		return allPathsFromTargetFileToRoot;
	}

	protected void reverseFileSearch(File rootFolder, Node node) throws IOException {
		File searchResult = node.currentItem;

		for (String parentFolderId : searchResult.getParents()) {
			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();

			Node nextNode = new Node();
			node.parentItems.add(nextNode);
			nextNode.currentItem = parentFolder;
			nextNode.nodeLevel = node.nodeLevel + 1;
			logger.info(String.format(
					"addingParentItemAsNextNodeToCurrentNodeParentItems.getName: %s getId: %s nodeLevel: %s",
					parentFolder.getName(), parentFolder.getId(), nextNode.nodeLevel));

			// when we reach the root folder, then we terminate the recursion
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}

			reverseFileSearch(rootFolder, nextNode);
		}
	}

	public List<File> findPathFromTargetFileToRoot(Queue<String> queue) throws IOException {
		String initialSearchItemName = queue.poll();
		
		if (!StringUtils.equals(initialSearchItemName, fileNameToSearch)) {
			throw new IllegalArgumentException(
					String.format("The first item in the search queue is the target file: %s "
					+ "This target file in the search queue does not match the search file that was provided during object instantiation: %s"
							, initialSearchItemName, fileNameToSearch));
		}
		
		List<Node> nodeList = findAllPathsFromTargetFileToRoot();
		List<File> returnFileList = new LinkedList<File>();

		if (nodeList != null && !nodeList.isEmpty()) {
			for (Node firstNode : nodeList) {
				// the initialSearchItemName could have multiple results, and we call
				// reverseFileCompare on each one.
				// because of this, we need to COPY the queue, as each individual
				// reverseFileCompare consumes the queue it receives.
				Queue<String> queueToConsume = new LinkedList<String>(queue);
				List<File> subsequentFileList = reverseFileCompare(queueToConsume, firstNode);

				if (CollectionUtils.isNotEmpty(subsequentFileList)) {
					returnFileList.add(firstNode.currentItem);
					returnFileList.addAll(subsequentFileList);
					break;
				}

				// if we get here, then the path from file to root was NOT found, so reset for
				// next "searchResult"
				returnFileList = new LinkedList<File>();
			}
		}

		return returnFileList;
	}

	private List<File> reverseFileCompare(Queue<String> queue, Node currentNode) throws IOException {
		List<File> returnFileList = new LinkedList<File>();

		String nextItemNameInQueue = queue.poll();

		for (Node parentNode : currentNode.parentItems) {
			File parentFolder = parentNode.currentItem;
			String parentFolderName = parentFolder.getName();

			if (parentFolderName.equals(nextItemNameInQueue)) {
				List<File> subsequentFileList = reverseFileCompare(queue, parentNode);

				if (CollectionUtils.isNotEmpty(subsequentFileList)) {
					returnFileList.add(parentFolder);
					returnFileList.addAll(subsequentFileList);
					break;
				}
				// why queue.isEmpty()? because the following situation is possible and would produce a 
				// false positive if we didn't check for the queue being empty.
				// say the file exists under a directory structure of [root] -> D -> E -> F -> [targetFile],
				// and furthermore say that the search queue has structure of
				// [root] -> A-> B-> C -> D -> E -> F -> [targetFile].
				// Well, as soon as we reverseFileCompare runs and we get D, then next recursive call of
				// reverseFileCompare will end on [root], and the recursion will end. However, the recursive 
				// search in that case would have incorrectly excluded A, B, and C from the check. 
				// By having the queue.isempty check, we ensure that this situation will not happen, as the
				// queue isn't empty until A, B, and C are successfully taken off the queue.
			} else if (rootFolder.getId().equals(parentFolder.getId()) && queue.isEmpty()) {
				returnFileList.add(rootFolder);
				break;
			}
		}
		
		return returnFileList;
	}
}
