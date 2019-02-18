package com.google.api.services.samples.drive.cmdline;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.samples.drive.cmdline.queries.DriveRecursiveFileSearchQueries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DriveQuickstart {
	private static Logger logger = LoggerFactory.getLogger(DriveQuickstart.class);

    private static final String APPLICATION_NAME = "Google Drive API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DriveQuickstart.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String... args) throws IOException, GeneralSecurityException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        
//		List<File> sanityCheckResults = sanityCheck(service);
//        outputListOfFileResults(sanityCheckResults);

//        DriveBasicReadQueries query = new DriveBasicReadQueries(service);
        
//        List<File> listFoldersInRootResults = query.listFoldersInRoot();
//        outputListOfFileResults(listFoldersInRootResults);
        
		DriveRecursiveFileSearchQueries recursiveSearch = new DriveRecursiveFileSearchQueries(service);
//		checkFilePath_1(recursiveSearch);
//		checkFilePath_2(recursiveSearch);
//		checkFilePath_3(recursiveSearch);
		recursiveSearch.findAllParentDirectories("Be40-notes");
	}
    
	private static List<File> sanityCheck(Drive service) throws IOException {
		// Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        return files;
	}
	
    private static void outputListOfFileResults(List<File> files) {
        if (files == null || files.isEmpty()) {
        	logger.info("No results.");
        } else {
        	logger.info("Files:");
            for (File file : files) {
            	logger.info(String.format("%s (%s)", file.getName(), file.getId()));
            }
		}
    }
    
	public static void checkFilePath_1(DriveRecursiveFileSearchQueries recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		List<File> actualResults = new LinkedList<File>();
		queue.offer("Be40-notes");
		queue.offer("Docs");
		queue.offer("CAE Simuflite Beechjet");
		queue.offer("Beechjet");
		queue.offer("DDA");
		queue.offer("CFI");
		queue.offer("Flying");
		boolean found = recursiveSearch.pathExistsFromFileToRoot(queue, actualResults);

		for (File file : actualResults) {
			logger.info("file " + file.getName());
		}

		logger.info("found " + found);
	}

	public static void checkFilePath_2(DriveRecursiveFileSearchQueries recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		List<File> actualResults = new ArrayList<File>();
		queue.offer("Be40-notes");
		queue.offer("Docs");
		queue.offer("CAE Simuflite Beechjet");
		queue.offer("Beechjet");
		queue.offer("Asus stuff");

		boolean found = recursiveSearch.pathExistsFromFileToRoot(queue, actualResults);

		for (File file : actualResults) {
			logger.info("file " + file.getName());
		}

		logger.info("found " + found);
	}

	private static void checkFilePath_3(DriveRecursiveFileSearchQueries recursiveSearch) throws IOException { // this points to the same file as in
														// doTheRecursion4
		Queue<String> queue = new LinkedList<String>();
		List<File> actualResults = new ArrayList<File>();
		queue.offer("Be40-notes");
		queue.offer("past-safety-messages");
		queue.offer("Flight club");

		boolean found = recursiveSearch.pathExistsFromFileToRoot(queue, actualResults);

		for (File file : actualResults) {
			logger.info("file " + file.getName());
		}

		logger.info("found " + found);
	}
}