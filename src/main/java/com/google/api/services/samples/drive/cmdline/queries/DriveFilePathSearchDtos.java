package com.google.api.services.samples.drive.cmdline.queries;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
		
		public Iterator<FilePath> getFilePaths() {
			return setOfFilePaths.iterator();
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
	}
}
