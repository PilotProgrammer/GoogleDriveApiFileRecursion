package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class TargetFilePathsDriveQuery {
	protected Drive service = null;
	protected File rootFolder = null;
	protected String targetFileName = null;
	protected List<ReverseNode> allReverseFilePaths = null;

	public TargetFilePathsDriveQuery(Drive service, String targetFileName) throws IOException {
		this.service = service;
		rootFolder = service.files().get("root").setFields("id, name").execute();
		this.targetFileName = targetFileName;
	}
	
	public static class Node {
		public File currentItem;
		public Node nextNode;
	}

	public static class ReverseNode {
		public File currentItem;
		public List<ReverseNode> parentItems = new ArrayList<ReverseNode>();
	}

	public List<ReverseNode> findFilePaths() throws IOException {
		// 1
		if (allReverseFilePaths != null) {
			return allReverseFilePaths;
		}
		
		// 2
		allReverseFilePaths = new ArrayList<ReverseNode>();
		
		// 3
		FileList result = service.files().list().setQ(String.format("name = '%s' and trashed = false", targetFileName))
				.setSpaces("drive").setFields("nextPageToken, files(id, name, parents)").execute();

		List<File> targetFiles = result.getFiles();
		for (File targetFile : targetFiles) {
			// 4
			ReverseNode rootNode = new ReverseNode();
			rootNode.currentItem = targetFile;
			// 5 -- this data structure can have multiple "branches", as the "parentItems property of a reverseNode is a list
			constructReverseFilePath(rootNode);
			allReverseFilePaths.add(rootNode);
		}
	
//		List<Node> allForwardFilePaths = new ArrayList<Node>();
//		allReverseFilePaths.forEach((reverseNode) -> {
//			// when you encounter MULTIPLE parents, you need to COPY the forwardNode branch as it is, and then 
//		});

		toBranches();
		

		return allReverseFilePaths;
	}
	
	public void toBranches() throws IOException {
		Queue<String> targetFilePath = null;
		String targetFileNameFromQueue = targetFilePath.poll();

		Map<File,Set<List<File>>> allForwardFilePaths = new HashMap<File,Set<List<File>>>();
		
		List<File> returnTargetFilePath = new LinkedList<File>();

		if (allReverseFilePaths != null && !allReverseFilePaths.isEmpty()) {
			for (ReverseNode targetFileNode : allReverseFilePaths) {
				File targetFile = targetFileNode.currentItem;
				
				Set<List<File>> setOfForwardPathsForTargetFile = new HashSet<List<File>>();
				Queue<String> queueToConsume = new LinkedList<String>(targetFilePath);
				List<File> subsequentTargetFilePath = toBranches2(queueToConsume, targetFileNode);

				if (CollectionUtils.isNotEmpty(subsequentTargetFilePath)) {
					returnTargetFilePath.add(targetFileNode.currentItem);
					returnTargetFilePath.addAll(subsequentTargetFilePath);
					break;
				}

				// if we get here, then the path from file to root was NOT found, so reset for
				// next "searchResult"
				returnTargetFilePath = new LinkedList<File>();
			}
		}

	}
	
	protected List<File> toBranches2(Queue<String> targetFileNameFromQueue, ReverseNode currentNode)
			throws IOException {
		List<File> returnTargetFilePath = new LinkedList<File>();
		String nextItemNameInPath = targetFileNameFromQueue.poll();

		for (ReverseNode parentNode : currentNode.parentItems) {
			File parentFolder = parentNode.currentItem;
			String parentFolderName = parentFolder.getName();

			if (parentFolderName.equals(nextItemNameInPath)) {
				List<File> subsequentFileList = validateTargetFilePath(targetFileNameFromQueue, parentNode);

				if (CollectionUtils.isNotEmpty(subsequentFileList)) {
					returnTargetFilePath.add(parentFolder);
					returnTargetFilePath.addAll(subsequentFileList);
					break;
				}

			} else if (rootFolder.getId().equals(parentFolder.getId()) && targetFileNameFromQueue.isEmpty()) {
				returnTargetFilePath.add(rootFolder);
				break;
			}
		}

		return returnTargetFilePath;
	}


	
	public List<File> validateTargetFilePath(Queue<String> targetFilePath) throws IOException {
		String targetFileNameFromQueue = targetFilePath.poll();

		if (!StringUtils.equals(targetFileNameFromQueue, targetFileName)) {
			throw new IllegalArgumentException(String.format(
					"The first item in the search queue is the target file: %s "
							+ "This target file in the search queue does not match the target search file that was provided during object instantiation: %s",
					targetFileNameFromQueue, targetFileName));
		}

		List<ReverseNode> nodeList = findFilePaths();
		List<File> returnTargetFilePath = new LinkedList<File>();

		if (nodeList != null && !nodeList.isEmpty()) {
			for (ReverseNode firstNode : nodeList) {
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

	protected void constructReverseFilePath(ReverseNode currentNode) throws IOException {
		File currentItem = currentNode.currentItem;

		// 1
		for (String parentFolderId : currentItem.getParents()) {
			// 2
			File parentFolder = service.files().get(parentFolderId).setFields("id, name, parents").execute();

			// 3
			ReverseNode nextNode = new ReverseNode();
			currentNode.parentItems.add(nextNode);
			nextNode.currentItem = parentFolder;
			
			// 4
			// when we reach the root folder, then we terminate the recursion
			if (rootFolder.getId().equals(parentFolderId)) {
				return;
			}

			// 5
			constructReverseFilePath(nextNode);
		}
	}

	protected List<File> validateTargetFilePath(Queue<String> targetFileNameFromQueue, ReverseNode currentNode)
			throws IOException {
		List<File> returnTargetFilePath = new LinkedList<File>();
		String nextItemNameInPath = targetFileNameFromQueue.poll();

		for (ReverseNode parentNode : currentNode.parentItems) {
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
