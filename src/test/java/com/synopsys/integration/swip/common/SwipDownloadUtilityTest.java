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

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.test.tool.TestLogger;
import com.synopsys.integration.util.CleanupZipExpander;

public class SwipDownloadUtilityTest {
    @Test
    @Ignore
    // ekerwin 2018-07-23 leaving this test as it can be useful in diagnosing
    // problems to actually perform the download, but it shouldn't run normally
    // in dev/ci environments.
    public void testActualDownload() {
        final String swipCLIDownloadPath = System.getenv("SWIP_CLI_DOWNLOAD_PATH");
        final File downloadTarget = new File(swipCLIDownloadPath);

        final IntLogger intLogger = new TestLogger();
        final SwipDownloadUtility swipDownloadUtility = SwipDownloadUtility.defaultUtility(intLogger, downloadTarget);

        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testInitialDownload() throws Exception {
        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        final IntLogger intLogger = new TestLogger();
        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testNotDownloadIfNotUpdatedOnServer() throws Exception {
        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.empty());

        final TestLogger intLogger = new TestLogger();

        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertFalse(swipCliPath.isPresent());
        Assert.assertTrue(intLogger.getOutputString().contains("skipping download"));
    }

    @Test
    public void testDownloadIfServerUpdated() throws Exception {
        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeGetRequestIfModifiedSince(Mockito.any(Request.class), Mockito.anyLong())).thenReturn(Optional.of(mockResponse));

        final TestLogger intLogger = new TestLogger();

        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final File installDirectory = new File(downloadTarget, SwipDownloadUtility.SWIP_CLI_INSTALL_DIRECTORY);
        installDirectory.mkdirs();
        installDirectory.deleteOnExit();

        // create a directory that should be deleted by the update download/extract code
        final File directoryOfPreviousExtraction = new File(installDirectory, "temp_swip_cli_version");
        directoryOfPreviousExtraction.mkdirs();
        Assert.assertTrue(directoryOfPreviousExtraction.isDirectory());
        Assert.assertTrue(directoryOfPreviousExtraction.exists());

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        Assert.assertTrue(swipCliPath.isPresent());
        Assert.assertTrue(swipCliPath.get().length() > 0);
        Assert.assertFalse(directoryOfPreviousExtraction.exists());
        Assert.assertTrue(intLogger.getOutputString().contains("There were items"));
        Assert.assertTrue(intLogger.getOutputString().contains("that are being deleted"));
    }

}
