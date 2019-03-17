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
import com.google.api.services.samples.drive.cmdline.queries.DriveFilePathSearchQuery;

import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

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
        
//        logger.info("////////// SANITY CHECK //////////");
//		List<File> sanityCheckResults = sanityCheck(service);
//        outputListOfFileResults(sanityCheckResults);
//
//        DriveBasicReadQueries query = new DriveBasicReadQueries(service);
//        
//        List<File> listFoldersInRootResults = query.listFoldersInRoot();
//        logger.info("////////// LIST ALL FOLDERS IN ROOT DIRECTORY //////////");
//        outputListOfFileResults(listFoldersInRootResults);
//        
//        Map<File, List<File>> listChildItemsOfFolderResults = query.listChildItemsOfFolder("CAE Simuflite Beechjet");
//        logger.info("////////// LIST ALL CHILD ITEMS OF FOLDERS MATCHING NAME //////////");
//        listChildItemsOfFolderResults.forEach((parentFolder, childItems) -> {
//        	logger.info(String.format("listing child items of folderName: %s folderId: %s", parentFolder.getName(), parentFolder.getId()));
//            outputListOfFileResults(childItems);
//        });
                  
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
		DriveFilePathSearchQuery recursiveSearch1 = new DriveFilePathSearchQuery(service, "identicalFile");
		recursiveSearch1.getFilePathsSearchResult();
		
		DriveFilePathSearchQuery recursiveSearch = new DriveFilePathSearchQuery(service, "differentFile");
		recursiveSearch.getFilePathsSearchResult();


//		checkFilePath_1(recursiveSearch);
//		checkFilePath_2(recursiveSearch);
//		checkFilePath_3(recursiveSearch);
//		checkFilePath_4(recursiveSearch);
//		checkFilePath_5_illegalArg(recursiveSearch);
		
        stopWatch.stop();
        System.out.println("Time: " + stopWatch.getTime());
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
            for (File file : files) {
            	logger.info(String.format("file.getName: %s getId: %s", file.getName(), file.getId()));
            }
		}
    }
    
    /*
	public static void checkFilePath_1(TargetFilePathsDriveQuery recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		queue.offer("identicalFile");
		queue.offer("folderC1");
		queue.offer("folderB1");
		queue.offer("folderA1");
		List<File> pathOfItemsFromTargetFileToRoot = recursiveSearch.validateTargetFilePath(queue);
        logger.info("////////// CHECKING THAT FILE PATH EXISTS EXAMPLE 1 //////////");
	    outputListOfFileResults(pathOfItemsFromTargetFileToRoot);
	    boolean found = CollectionUtils.isNotEmpty(pathOfItemsFromTargetFileToRoot);
		logger.info("found " + found);
	}

	public static void checkFilePath_2(TargetFilePathsDriveQuery recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		queue.offer("identicalFile");
		queue.offer("folderC2");
		queue.offer("folderB2");
		queue.offer("folderA2");

		List<File> pathOfItemsFromTargetFileToRoot = recursiveSearch.validateTargetFilePath(queue);
        logger.info("////////// CHECKING THAT FILE PATH EXISTS EXAMPLE 2 //////////");
	    outputListOfFileResults(pathOfItemsFromTargetFileToRoot);
	    boolean found = CollectionUtils.isNotEmpty(pathOfItemsFromTargetFileToRoot);
		logger.info("found " + found);
	}

	private static void checkFilePath_3(TargetFilePathsDriveQuery recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		queue.offer("differentFile");
		queue.offer("folderC1");
		queue.offer("folderB1");
		queue.offer("folderA1");

		List<File> pathOfItemsFromTargetFileToRoot = recursiveSearch.validateTargetFilePath(queue);
        logger.info("////////// CHECKING THAT FILE PATH EXISTS EXAMPLE 3 //////////");
		outputListOfFileResults(pathOfItemsFromTargetFileToRoot);
	    boolean found = CollectionUtils.isNotEmpty(pathOfItemsFromTargetFileToRoot);
		logger.info("found " + found);
	}
	
	private static void checkFilePath_4(TargetFilePathsDriveQuery recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		queue.offer("differentFile");
		queue.offer("folderC2");
		queue.offer("folderB2");
		queue.offer("folderA2");

		List<File> pathOfItemsFromTargetFileToRoot = recursiveSearch.validateTargetFilePath(queue);
        logger.info("////////// CHECKING THAT FILE PATH EXISTS EXAMPLE 4 //////////");
		outputListOfFileResults(pathOfItemsFromTargetFileToRoot);
	    boolean found = CollectionUtils.isNotEmpty(pathOfItemsFromTargetFileToRoot);
		logger.info("found " + found);
	}
	
	private static void checkFilePath_5_illegalArg(TargetFilePathsDriveQuery recursiveSearch) throws IOException {
		Queue<String> queue = new LinkedList<String>();
		queue.offer("Be40-notes-wrong-file-should-throw-illegal-argument-exception");
		queue.offer("past-safety-messages");
		queue.offer("Flight club");
		
		List<File> pathOfItemsFromTargetFileToRoot = null;
		
		try {
			pathOfItemsFromTargetFileToRoot = recursiveSearch.validateTargetFilePath(queue);
		} catch (IllegalArgumentException ex) {
			logger.error(ex.getMessage());
		}
		
        logger.info("////////// CHECKING THAT FILE PATH EXISTS EXAMPLE 5 //////////");
		outputListOfFileResults(pathOfItemsFromTargetFileToRoot);
	    boolean found = CollectionUtils.isNotEmpty(pathOfItemsFromTargetFileToRoot);
		logger.info("found " + found);
	}
	*/
}