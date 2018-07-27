package com.synopsys.integration.swip.common;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;
import com.blackducksoftware.integration.test.tool.TestLogger;
import com.blackducksoftware.integration.util.CleanupZipExpander;

public class SwipDownloadUtilityTest {
    @Test
    @Ignore
    // ekerwin 2018-07-23 leaving this test as it can be useful in diagnosing
    // problems to actually perform the download, but it shouldn't run normally
    // in dev/ci environments.
    public void testActualDownload() {
        String swipCLIDownloadPath = System.getenv("SWIP_CLI_DOWNLOAD_PATH");
        File downloadTarget = new File(swipCLIDownloadPath);

        IntLogger intLogger = new TestLogger();
        SwipDownloadUtility swipDownloadUtility = SwipDownloadUtility.defaultUtility(intLogger, downloadTarget);

        Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testInitialDownload() throws Exception {
        InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        IntLogger intLogger = new TestLogger();
        Path tempDirectory = Files.createTempDirectory(null);
        File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testNotDownloadIfNotUpdatedOnServer() throws Exception {
        RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.empty());

        TestLogger intLogger = new TestLogger();

        Path tempDirectory = Files.createTempDirectory(null);
        File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertFalse(swipCliPath.isPresent());
        Assert.assertTrue(intLogger.getOutputString().contains("skipping download"));
    }

    @Test
    public void testDownloadIfServerUpdated() throws Exception {
        InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        TestLogger intLogger = new TestLogger();

        Path tempDirectory = Files.createTempDirectory(null);
        File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        File installDirectory = new File(downloadTarget, SwipDownloadUtility.SWIP_CLI_INSTALL_DIRECTORY);
        installDirectory.mkdirs();
        installDirectory.deleteOnExit();

        // create a directory that should be deleted by the update download/extract code
        File directoryOfPreviousExtraction = new File(installDirectory, "temp_swip_cli_version");
        directoryOfPreviousExtraction.mkdirs();
        Assert.assertTrue(directoryOfPreviousExtraction.isDirectory());
        Assert.assertTrue(directoryOfPreviousExtraction.exists());

        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
        Assert.assertFalse(directoryOfPreviousExtraction.exists());
        Assert.assertTrue(intLogger.getOutputString().contains("There were items"));
        Assert.assertTrue(intLogger.getOutputString().contains("that are being deleted"));
    }

}
