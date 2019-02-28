package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class TargetFilePathsDriveQuery {
	protected Drive service = null;
	protected File rootFolder = null;
	protected String targetFileName = null;
	protected List<Node> allTargetFilePaths = null;

	public TargetFilePathsDriveQuery(Drive service, String targetFileName) throws IOException {
		this.service = service;
		rootFolder = service.files().get("root").setFields("id, name").execute();
		this.targetFileName = targetFileName;
	}

	public static class Node {
		public File currentItem;
		public List<Node> parentItems = new ArrayList<Node>();
	}

	public List<Node> findAllTargetFilePaths() throws IOException {
		// 1
		if (allTargetFilePaths != null) {
			return allTargetFilePaths;
		}
		
		// 2
		allTargetFilePaths = new ArrayList<Node>();
		
		// 3
		FileList result = service.files().list().setQ(String.format("name = '%s' and trashed = false", targetFileName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> targetFiles = result.getFiles();
		for (File targetFile : targetFiles) {
			// 4
			Node rootNode = new Node();
			rootNode.currentItem = targetFile;
			// 5
			constructTargetFilePath(rootNode);
			allTargetFilePaths.add(rootNode);
		}

		return allTargetFilePaths;
	}
	
	public List<File> validateTargetFilePath(Queue<String> targetFilePath) throws IOException {
		String targetFileNameFromQueue = targetFilePath.poll();

		if (!StringUtils.equals(targetFileNameFromQueue, targetFileName)) {
			throw new IllegalArgumentException(String.format(
					"The first item in the search queue is the target file: %s "
							+ "This target file in the search queue does not match the target search file that was provided during object instantiation: %s",
					targetFileNameFromQueue, targetFileName));
		}

		List<Node> nodeList = findAllTargetFilePaths();
		List<File> returnTargetFilePath = new LinkedList<File>();

		if (nodeList != null && !nodeList.isEmpty()) {
			for (Node firstNode : nodeList) {
				// the targetFilePath could have multiple results, and we call
				// validateTargetFilePath on each one.
				// because of this, we need to COPY the queue, as the
				// initial call to validateTargetFilePath for each new firstNode
				// consumes the queue it receives.
				Queue<String> queueToConsume = new LinkedList<String>(targetFilePath);
				List<File> subsequentTargetFilePath = validateTargetFilePath(queueToConsume, firstNode);

				if (CollectionUtils.isNotEmpty(subsequentTargetFilePath)) {
					returnTargetFilePath.add(firstNode.currentItem);
					returnTargetFilePath.addAll(subsequentTargetFilePath);
					break;
				}

				// if we get here, then the path from file to root was NOT found, so reset for
				// next "searchResult"
				returnTargetFilePath = new LinkedList<File>();
			}
		}

		return returnTargetFilePath;
	}

	protected void constructTargetFilePath(Node node) throws IOException {
		File searchResult = node.currentItem;

		// 1
		for (String parentFolderId : searchResult.getParents()) {
			// 2
			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();

			// 3
			Node nextNode = new Node();
			node.parentItems.add(nextNode);
			nextNode.currentItem = parentFolder;
			
			// 4
			// when we reach the root folder, then we terminate the recursion
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}

			// 5
			constructTargetFilePath(nextNode);
		}
	}

	private List<File> validateTargetFilePath(Queue<String> targetFileNameFromQueue, Node currentNode)
			throws IOException {
		List<File> returnTargetFilePath = new LinkedList<File>();
		String nextItemNameInPath = targetFileNameFromQueue.poll();

		for (Node parentNode : currentNode.parentItems) {
			File parentFolder = parentNode.currentItem;
			String parentFolderName = parentFolder.getName();

			if (parentFolderName.equals(nextItemNameInPath)) {
				List<File> subsequentFileList = validateTargetFilePath(targetFileNameFromQueue, parentNode);

				if (CollectionUtils.isNotEmpty(subsequentFileList)) {
					returnTargetFilePath.add(parentFolder);
					returnTargetFilePath.addAll(subsequentFileList);
					break;
				}
				/*
				 * why queue.isEmpty()? because the following situation is possible and would
				 * produce a false positive if we didn't check for the queue being empty. say
				 * the file exists under a directory structure of [root] -> D -> E -> F ->
				 * [targetFile], and furthermore say that the search queue has structure of
				 * [root] -> A-> B-> C -> D -> E -> F -> [targetFile]. Well, as soon as we
				 * reverseFileCompare runs and we get D, then next recursive call of
				 * reverseFileCompare will end on [root], and the recursion will end. However,
				 * the recursive search in that case would have incorrectly excluded A, B, and C
				 * from the check. By having the queue.isempty check, we ensure that this
				 * situation will not happen, as the queue isn't empty until A, B, and C are
				 * successfully taken off the queue.
				 */
			} else if (rootFolder.getId().equals(parentFolder.getId()) && targetFileNameFromQueue.isEmpty()) {
				returnTargetFilePath.add(rootFolder);
				break;
			}
		}

		return returnTargetFilePath;
	}
}
