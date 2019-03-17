package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class DriveFilePathSearchDtos {
	public static class ReverseNode {
		public File currentItem;
		public List<ReverseNode> parentItems = new ArrayList<ReverseNode>();
	}

	public static class FilePath {
		private Queue<File> queue = new LinkedList<File>();

		public void addFile(File file) {
			queue.add(file);
		}

		public File getNextFile() {
			return queue.poll();
		}

		@Override
		public String toString() {
			String message = String.format("~~~~~~~ %s ~~~~~~~ \n", this.getClass().getName());
			StringBuilder sb = new StringBuilder(message);

			for (File file : queue) {
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

		@Override
		public String toString() {
			String baseMessageFormat = "~~~~~~~ %s %s for File with id: %s name: %s ~~~~~~~ \n";
			String startMessage = String.format(baseMessageFormat + "setOfFilePaths: ", "START",
					this.getClass().getName(), file.getId(), file.getName());
			StringBuilder sb = new StringBuilder(startMessage);
			for (FilePath filePath : setOfFilePaths) {
				sb.append(filePath.toString());
			}
			String endMessage = String.format(baseMessageFormat, "END",
					this.getClass().getName(), file.getId(), file.getName());
			sb.append(endMessage);
			return sb.toString();
		}
	}

	public static class FilePathsSearchResult {
		protected String targetFileName;
		protected List<FilePathCollection> filePathCollections;

		public FilePathsSearchResult(String targetFileName) {
			this.targetFileName = targetFileName;
			filePathCollections = new ArrayList<FilePathCollection>();
		}

		public String getTargetFileName() {
			return targetFileName;
		}

		public List<FilePathCollection> getFileResults() {
			return filePathCollections;
		}

		public void addFilePathCollection(FilePathCollection filePathCollection) {
			filePathCollections.add(filePathCollection);
		}

		public boolean checkFilePathExists(Queue<String> filePathOfDirectoryNames) {
			return true;
		}
	}
}
