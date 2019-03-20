package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.model.File;

import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DriveFilePathSearchDtos {
	public static class ReverseNode {
		public File currentItem;
		public List<ReverseNode> parentItems = new ArrayList<ReverseNode>();
	}

	public static class FilePath {
		private List<File> filePath = new LinkedList<File>();

		public void addFile(File file) {
			filePath.add(file);
		}

		public List<File> getFilePath() {
			return filePath;
		}
		
		public boolean equalsFilePath(List<File> filePathToCompare) {
			boolean areEqual = true;

			if (CollectionUtils.isEmpty(filePathToCompare)) {
				areEqual = false;
			}
			
			if (filePathToCompare.size() != filePath.size()) {
				areEqual = false;
			} else {
				for (int idx = 0; idx < filePath.size(); idx++) {
					File file = filePath.get(idx);
					File fileToCompare = filePathToCompare.get(idx);
					
					if (!file.getId().equals(fileToCompare.getId())) {
						areEqual = false;
						break;
					}
				}
			}
			
			return areEqual;
		}
		
		public boolean equalsStringPath(List<String> filePathToCompare) {
			boolean areEqual = true;

			if (CollectionUtils.isEmpty(filePathToCompare)) {
				areEqual = false;
			}
			
			if (filePathToCompare.size() != filePath.size()) {
				areEqual = false;
			} else {
				for (int idx = 0; idx < filePath.size(); idx++) {
					File file = filePath.get(idx);
					String fileToCompare = filePathToCompare.get(idx);
					
					if (!file.getName().equals(fileToCompare)) {
						areEqual = false;
						break;
					}
				}
			}
			
			return areEqual;		
		}
		
		@Override
		public String toString() {
			String message = String.format("~~~~~~~ %s ~~~~~~~ \n", this.getClass().getSimpleName());
			StringBuilder sb = new StringBuilder(message);

			for (File file : filePath) {
				sb.append(String.format("id: %s name: %s \n", file.getId(), file.getName()));
			}

			return sb.toString();
		}
	}

	public static class FilePathCollection {
		private File file;
		private Set<FilePath> setOfFilePaths = new HashSet<FilePath>();

		public FilePathCollection(File file) {
			this.file = file;
		}

		public File getFile() {
			return file;
		}

		public void addFilePath(FilePath filePath) {
			setOfFilePaths.add(filePath);
		}

		public Collection<FilePath> getFilePaths() {
			return setOfFilePaths;
		}

		public boolean hasFilePaths() {
			return setOfFilePaths.size() > 0;
		}

		public void addAllFilePaths(FilePathCollection filePathCollection) {
			setOfFilePaths.addAll(filePathCollection.getFilePaths());
		}
		
		public boolean containsFilePath(List<String> filePath) {
			boolean contains = false;
		
			for (FilePath fPath: setOfFilePaths) {
				if (fPath.equalsStringPath(filePath)) {
					contains = true;
					break;
				}
			}
			
			return contains;
		}

		@Override
		public String toString() {
			String baseMessageFormat = "/////////////// %s %s for File with id: %s name: %s /////////////// \n";
			String startMessage = String.format(baseMessageFormat + "setOfFilePaths: ", "START",
					this.getClass().getSimpleName(), file.getId(), file.getName());
			StringBuilder sb = new StringBuilder(startMessage);
			for (FilePath filePath : setOfFilePaths) {
				sb.append(filePath.toString());
			}
			String endMessage = String.format(baseMessageFormat, "END",
					this.getClass().getSimpleName(), file.getId(), file.getName());
			sb.append(endMessage + "\n");
			return sb.toString();
		}
	}

	public static class FilePathsSearchResult {
		protected String searchFileName;
		protected List<FilePathCollection> filePathCollections;

		public FilePathsSearchResult(String searchFileName) {
			this.searchFileName = searchFileName;
			filePathCollections = new ArrayList<FilePathCollection>();
		}

		public String getSearchFileName() {
			return searchFileName;
		}

		public List<FilePathCollection> getFileResults() {
			return filePathCollections;
		}

		public void addFilePathCollection(FilePathCollection filePathCollection) {
			filePathCollections.add(filePathCollection);
		}

		public boolean checkFilePathExists(List<String> searchFilePath) {
			boolean exists = false;

			if (!CollectionUtils.isEmpty(searchFilePath)) {			
				for (FilePathCollection collection : filePathCollections) {
					if (collection.containsFilePath(searchFilePath)) {
						exists = true;
						break;
					}
				}
			}

			return exists;
		}
		
		@Override
		public String toString() {
			String baseMessageFormat = "///////////////************ %s %s for searchFileName: %s ************/////////////// \n";
			String startMessage = String.format(baseMessageFormat + "setOfFilePaths: ", "START",
					this.getClass().getSimpleName(), searchFileName);
			StringBuilder sb = new StringBuilder(startMessage);
			for (FilePathCollection filePathCollection : filePathCollections) {
				sb.append(filePathCollection.toString());
			}
			String endMessage = String.format(baseMessageFormat, "END",
					this.getClass().getSimpleName(), searchFileName);
			sb.append(endMessage + "\n");
			return sb.toString();
		}
	}
}
