package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.samples.drive.cmdline.queries.DriveFilePathSearchDtos.ReverseNode;

import org.apache.commons.collections4.CollectionUtils;

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
	protected Map<File,Set<Queue<File>>> branchesForAllFilesMatchingTargetName = null;

	public TargetFilePathsDriveQuery(Drive service, String targetFileName) throws IOException {
		this.service = service;
		
		if (service != null) {
			rootFolder = service.files().get("root").setFields("id, name").execute();
		} else {
			throw new IllegalArgumentException("Drive Service must not be null");
		}
		
		this.targetFileName = targetFileName;
	}
		

	public Map<File,Set<Queue<File>>> getFilePaths() throws IOException {
		// 1
		if (branchesForAllFilesMatchingTargetName != null) {
			return branchesForAllFilesMatchingTargetName;
		}
		
		// 2
		List<ReverseNode> allReverseFilePaths = new ArrayList<ReverseNode>();
		
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

		branchesForAllFilesMatchingTargetName = mapReverseTreeToForwardBranchesForFiles(allReverseFilePaths);
		return branchesForAllFilesMatchingTargetName;
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
	
	public Map<File,Set<Queue<File>>> mapReverseTreeToForwardBranchesForFiles(List<ReverseNode> allReverseFilePaths) throws IOException {
		Map<File,Set<Queue<File>>> branchesForAllTargetFiles = new HashMap<File,Set<Queue<File>>>();
		
		
		if (allReverseFilePaths != null && !allReverseFilePaths.isEmpty()) {
			// this outer loop executes once for each target file. each target file can have multiple paths.
			for (ReverseNode targetFileNode : allReverseFilePaths) {
				File targetFile = targetFileNode.currentItem; // used for key in allForwardFilePaths var
				Set<Queue<File>> setOfForwardPathsForTargetFile = mapReverseTreeToForwardBranchesForFile(targetFileNode);

				if (CollectionUtils.isNotEmpty(setOfForwardPathsForTargetFile)) {					
					branchesForAllTargetFiles.put(targetFile, setOfForwardPathsForTargetFile);
				}
			}
		}
		
		return branchesForAllTargetFiles;
	}
	
	protected Set<Queue<File>> mapReverseTreeToForwardBranchesForFile(ReverseNode currentNode)
			throws IOException {
		Set<Queue<File>> aggregateSetOfFilePaths = new HashSet<Queue<File>>();
		
		// this means that we have reached the root / "My Drive" folder, 
		// in which case we instantiate a list with the root as only parent,
		// add it to a newly instantiated set.
		if (CollectionUtils.isEmpty(currentNode.parentItems)) {
			Queue<File> filePathList = new LinkedList<File>();
			filePathList.add(currentNode.currentItem);
			aggregateSetOfFilePaths.add(filePathList);
		} else {
			// we have NOT reached the root node, so we need to check all 
			// the parent items and recursively build the 
			// reverse file paths
			// TODOGG - article - mention this is essentially DFS
			for (ReverseNode parentNode : currentNode.parentItems) {
				Set<Queue<File>> subsequentSetOfForwardPaths = mapReverseTreeToForwardBranchesForFile(parentNode);
				aggregateSetOfFilePaths.addAll(subsequentSetOfForwardPaths);
			}
			
			// now that we have completed DFS for all the branches related to THIS File, it's time to actually 
			// add this item to the file path.
			if (CollectionUtils.isNotEmpty(aggregateSetOfFilePaths)) {
				for (Queue<File> queue : aggregateSetOfFilePaths) {
					queue.add(currentNode.currentItem);
				}
			}
		}

		return aggregateSetOfFilePaths;
	}
}