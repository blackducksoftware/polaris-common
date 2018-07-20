/**
 * swip-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.swip.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.examples.Expander;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.HttpMethod;
import com.blackducksoftware.integration.rest.connection.UnauthenticatedRestConnection;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;

public class SwipDownloadUtility {
    public static final String DEFAULT_SWIP_SERVER_URL = "https://tools.swip.synopsys.com";

    public static final String LINUX_DOWNLOAD_URL = "/swip_cli-linux64.zip";
    public static final String WINDOWS_DOWNLOAD_URL = "/swip_cli-win64.zip";
    public static final String MAC_DOWNLOAD_URL = "/swip_cli-macosx.zip";

    public static final String SWIP_CLI_INSTALL_DIRECTORY = "Swip_CLI_Installation";
    public static final String VERSION_FILENAME = "swipVersion.txt";

    private final IntLogger logger;
    private final String swipServerUrl;
    private final File installDirectory;

    /**
     * Given a valid Swip url and a valid download directory, you can
     * getSwipCliExecutablePath() which will, if necessary, download the Swip
     * CLI from the server and provide you the correct path to the executable.
     */
    public SwipDownloadUtility(IntLogger logger, String swipServerUrl, File downloadTargetDirectory) {
        if (StringUtils.isBlank(swipServerUrl)) {
            throw new IllegalArgumentException("A Swip server url must be provided.");
        }

        this.logger = logger;
        this.swipServerUrl = swipServerUrl;
        installDirectory = new File(downloadTargetDirectory, SWIP_CLI_INSTALL_DIRECTORY);

        installDirectory.mkdirs();
        if (!installDirectory.exists() || !installDirectory.isDirectory() || !installDirectory.canWrite()) {
            throw new IllegalArgumentException("The provided directory must exist and be writable.");
        }
    }

    /**
     * The Swip CLI will be download if it has not previously been downloaded or
     * if it has been updated on the server. The absolute path to the swip_cli
     * executable will be returned if it was downloaded or found successfully,
     * otherwise an Optional.empty will be returned and the log will contain
     * details concerning the failure.
     */
    public Optional<String> getSwipCliExecutablePath() {
        File versionFile = null;
        try {
            versionFile = getVersionFile();
        } catch (IOException e) {
            logger.error("Could not create the version file: " + e.getMessage());
            return Optional.empty();
        }

        String downloadUrl = getDownloadUrl();
        return getSwipCliExecutablePath(versionFile, downloadUrl);
    }

    public Optional<String> getSwipCliExecutablePath(File versionFile, String downloadUrl) {
        File binDirectory = null;
        try {
            binDirectory = downloadIfNotModified(versionFile, downloadUrl);
        } catch (Exception e) {
            logger.error("The Swip CLI could not be downloaded successfully: " + e.getMessage());
        }

        if (binDirectory != null && binDirectory.exists() && binDirectory.isDirectory()) {
            try {
                File swipCliExecutable = getSwipCli(binDirectory);
                logger.info("Swip CLI downloaded/found successfully: " + swipCliExecutable.getCanonicalPath());
                return Optional.of(swipCliExecutable.getCanonicalPath());
            } catch (Exception e) {
                logger.error("The Swip CLI executable could not be found: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    public File getVersionFile() throws IOException {
        File versionFile = new File(installDirectory, VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    public String getDownloadUrl() {
        if (SystemUtils.IS_OS_MAC) {
            return swipServerUrl + MAC_DOWNLOAD_URL;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return swipServerUrl + WINDOWS_DOWNLOAD_URL;
        } else {
            return swipServerUrl + LINUX_DOWNLOAD_URL;
        }
    }

    private File downloadIfNotModified(File versionFile, String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        Request.Builder requestBuilder = new Request.Builder(downloadUrl).method(HttpMethod.HEAD);
        Request headRequest = requestBuilder.build();

        requestBuilder.method(HttpMethod.GET);
        Request request = requestBuilder.build();

        UnauthenticatedRestConnection unauthenticatedRestConnection = new UnauthenticatedRestConnection(logger, null, 120, ProxyInfo.NO_PROXY_INFO);

        long lastModifiedOnServer = 0L;
        try (Response headResponse = unauthenticatedRestConnection.executeRequest(headRequest)) {
            lastModifiedOnServer = headResponse.getLastModified();
            logger.debug(String.format("Last modified on server: %d", lastModifiedOnServer));
        } catch (IntegrationException e) {
            logger.error("Couldn't get the Last-Modified header from the server.");
            throw e;
        }

        if (lastModifiedOnServer == lastTimeDownloaded) {
            logger.debug("The Swip CLI has not been modified since it was last downloaded - skipping download.");
            String swipCliDirectoryName = FileUtils.readFileToString(versionFile, StandardCharsets.UTF_8);
            return getBinDirectory();
        }

        // it is important to first create the zip file as a stream cannot be
        // unzipped correctly in all cases
        // "If possible, you should always prefer ZipFile over
        // ZipArchiveInputStream."
        // https://commons.apache.org/proper/commons-compress/zip.html#ZipArchiveInputStream_vs_ZipFile
        File tempZipFile = File.createTempFile("tmpzip", null);
        try (Response response = unauthenticatedRestConnection.executeRequest(request)) {
            logger.info("Downloading the Swip CLI.");

            try (InputStream responseStream = response.getContent(); FileOutputStream fileOutputStream = new FileOutputStream(tempZipFile)) {
                IOUtils.copy(responseStream, fileOutputStream);
            }
        } catch (IntegrationException e) {
            throw e;
        }

        if (!tempZipFile.exists() || tempZipFile.length() <= 0) {
            throw new IntegrationException("The zip file was not downloaded correctly. Please try again.");
        }

        Expander expander = new Expander();
        try {
            expander.expand(tempZipFile, installDirectory);
        } catch (IOException | ArchiveException e) {
            logger.error("Couldn't extract the zip file - check the file's permissions: " + e.getMessage());
            throw e;
        }

        FileUtils.deleteQuietly(tempZipFile);

        versionFile.setLastModified(lastModifiedOnServer);

        File binDirectory = getBinDirectory();
        makeBinFilesExecutable(binDirectory);

        logger.info(String.format("Swip CLI downloaded successfully."));

        return binDirectory;
    }

    // since we know that we only allow a single directory in installDirectory,
    // that single directory IS the extracted archive
    private File getBinDirectory() throws IntegrationException {
        File[] directories = installDirectory.listFiles(file -> file.isDirectory());
        if (directories.length != 1) {
            throw new IntegrationException(String.format("The %s directory should only be modified by swip-common. Please delete all files from that directory and try again.", SWIP_CLI_INSTALL_DIRECTORY));
        }

        File swipCliDirectory = directories[0];
        File bin = new File(swipCliDirectory, "bin");

        return bin;
    }

    private void makeBinFilesExecutable(File binDirectory) {
        Arrays.stream(binDirectory.listFiles()).forEach(file -> {
            file.setExecutable(true);
        });
    }

    private File getSwipCli(File binDirectory) throws IntegrationException {
        String swipCliFilename = "swip_cli";
        if (SystemUtils.IS_OS_WINDOWS) {
            swipCliFilename += ".exe";
        }

        File swipCli = new File(binDirectory, swipCliFilename);

        if (!swipCli.exists() || !swipCli.isFile() || !(swipCli.length() > 0L)) {
            throw new IntegrationException("The swip_cli does not appear to have been downloaded correctly - be sure to download it first.");
        }

        return swipCli;
    }

}
