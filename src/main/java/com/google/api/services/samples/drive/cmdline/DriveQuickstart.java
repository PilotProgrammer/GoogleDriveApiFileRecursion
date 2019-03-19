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
import com.google.api.services.samples.drive.cmdline.queries.DriveFilePathQuery;
import com.google.api.services.samples.drive.cmdline.queries.DriveFilePathSearchDtos.FilePathsSearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
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
                          

		String[] filePathArray = { "My Drive", "folderA1", "folderB1", "folderC1", "folderD", "identicalFile" };
		String[] filePathArray2 = { "My Drive", "folderA2", "folderB2", "folderC2", "folderD", "identicalFile" };
		String[] filePathArray3 = { "My Drive", "folderA1", "folderB1", "folderC1", "folderE1", "differentFile" };
		String[] filePathArray4 = { "My Drive", "folderA2", "folderB2", "folderC2", "folderE2", "differentFile" };

		DriveFilePathQuery identicalFileQuery = new DriveFilePathQuery(service, "identicalFile");
		FilePathsSearchResult identicalFileResult = identicalFileQuery.getFilePathsSearchResult();

		DriveFilePathQuery differentFileQuery = new DriveFilePathQuery(service, "differentFile");
		FilePathsSearchResult differentFileResult = differentFileQuery.getFilePathsSearchResult();

		checkExists(identicalFileResult, filePathArray);
		checkExists(identicalFileResult, filePathArray2);
		checkExists(differentFileResult, filePathArray3);
		checkExists(differentFileResult, filePathArray4);

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
	    
	public static void checkExists(FilePathsSearchResult result, String[] filePathArray) {
		List<String> filePathList = Arrays.asList(filePathArray);
	    boolean found = result.checkFilePathExists(filePathList);
		logger.info(String.format("FilePath: [%s] exists: %s", String.join("]/[", filePathArray), found));
	}
}