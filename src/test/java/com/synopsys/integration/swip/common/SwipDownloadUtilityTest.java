package com.synopsys.integration.swip.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.SilentIntLogger;
import com.synopsys.integration.rest.connection.RestConnection;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;

public class SwipDownloadUtilityTest {
    @Test
    public void testActualDownload() {
        final String swipCLIDownloadPath = System.getenv("SWIP_CLI_DOWNLOAD_PATH");
        assumeTrue(StringUtils.isNotBlank(swipCLIDownloadPath));
        final File downloadTarget = new File(swipCLIDownloadPath);

        final IntLogger intLogger = new SilentIntLogger();
        final SwipDownloadUtility swipDownloadUtility = SwipDownloadUtility.defaultUtility(intLogger, downloadTarget);

        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();
        assertTrue(swipCliPath.isPresent());
        assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testInitialDownload() throws Exception {
        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);
        Mockito.when(mockResponse.getLastModified()).thenReturn(Long.MAX_VALUE);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeRequest(Mockito.any(Request.class))).thenReturn(mockResponse);

        final IntLogger intLogger = new SilentIntLogger();
        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        assertTrue(swipCliPath.isPresent());
        assertTrue(swipCliPath.get().length() > 0);
    }

    @Test
    public void testNotDownloadIfNotUpdatedOnServer() throws Exception {
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getLastModified()).thenReturn(0L);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeRequest(Mockito.any(Request.class))).thenReturn(mockResponse);

        final BufferedIntLogger intLogger = new BufferedIntLogger();

        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        assertFalse(swipCliPath.isPresent());
        assertTrue(intLogger.getOutputString(LogLevel.DEBUG).contains("skipping download"));
    }

    @Test
    public void testDownloadIfServerUpdated() throws Exception {
        final InputStream zipFileStream = getClass().getResourceAsStream("/swip_mac.zip");
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.when(mockResponse.getContent()).thenReturn(zipFileStream);
        Mockito.when(mockResponse.getLastModified()).thenReturn(Long.MAX_VALUE);

        final RestConnection mockRestConnection = Mockito.mock(RestConnection.class);
        Mockito.when(mockRestConnection.executeRequest(Mockito.any(Request.class))).thenReturn(mockResponse);

        final BufferedIntLogger intLogger = new BufferedIntLogger();

        final Path tempDirectory = Files.createTempDirectory(null);
        final File downloadTarget = tempDirectory.toFile();
        downloadTarget.deleteOnExit();

        final File installDirectory = new File(downloadTarget, SwipDownloadUtility.SWIP_CLI_INSTALL_DIRECTORY);
        installDirectory.mkdirs();
        installDirectory.deleteOnExit();

        // create a directory that should be deleted by the update download/extract code
        final File directoryOfPreviousExtraction = new File(installDirectory, "temp_swip_cli_version");
        directoryOfPreviousExtraction.mkdirs();
        assertTrue(directoryOfPreviousExtraction.isDirectory());
        assertTrue(directoryOfPreviousExtraction.exists());

        final CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(intLogger);
        final SwipDownloadUtility swipDownloadUtility = new SwipDownloadUtility(intLogger, mockRestConnection, cleanupZipExpander, SwipDownloadUtility.DEFAULT_SWIP_SERVER_URL, downloadTarget);
        final Optional<String> swipCliPath = swipDownloadUtility.retrieveSwipCliExecutablePath();

        assertTrue(swipCliPath.isPresent());
        assertTrue(swipCliPath.get().length() > 0);
        assertFalse(directoryOfPreviousExtraction.exists());
        assertTrue(intLogger.getOutputString(LogLevel.WARN).contains("There were items"));
        assertTrue(intLogger.getOutputString(LogLevel.WARN).contains("that are being deleted"));
    }

}
