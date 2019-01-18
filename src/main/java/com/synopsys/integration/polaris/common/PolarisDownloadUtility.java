/**
 * polaris-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.polaris.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.CleanupZipExpander;

public class PolarisDownloadUtility {
    public static final String DEFAULT_POLARIS_SERVER_URL = "https://tools.swip.synopsys.com";
    public static final Integer DEFAULT_POLARIS_TIMEOUT = 120;

    public static final String LINUX_DOWNLOAD_URL = "/swip_cli-linux64.zip";
    public static final String WINDOWS_DOWNLOAD_URL = "/swip_cli-win64.zip";
    public static final String MAC_DOWNLOAD_URL = "/swip_cli-macosx.zip";

    public static final String POLARIS_CLI_INSTALL_DIRECTORY = "Polaris_CLI_Installation";
    public static final String VERSION_FILENAME = "polarisVersion.txt";

    private final IntLogger logger;
    private final IntHttpClient intHttpClient;
    private final CleanupZipExpander cleanupZipExpander;
    private final String polarisServerUrl;
    private final File installDirectory;

    public static PolarisDownloadUtility defaultUtility(IntLogger logger, File downloadTargetDirectory) {
        IntHttpClient intHttpClient = new IntHttpClient(logger, PolarisDownloadUtility.DEFAULT_POLARIS_TIMEOUT, false, ProxyInfo.NO_PROXY_INFO);
        CleanupZipExpander cleanupZipExpander = new CleanupZipExpander(logger);
        return new PolarisDownloadUtility(logger, intHttpClient, cleanupZipExpander, PolarisDownloadUtility.DEFAULT_POLARIS_SERVER_URL, downloadTargetDirectory);
    }

    public PolarisDownloadUtility(IntLogger logger, IntHttpClient intHttpClient, CleanupZipExpander cleanupZipExpander, String polarisServerUrl, File downloadTargetDirectory) {
        if (StringUtils.isBlank(polarisServerUrl)) {
            throw new IllegalArgumentException("A Polaris server url must be provided.");
        }

        this.logger = logger;
        this.intHttpClient = intHttpClient;
        this.cleanupZipExpander = cleanupZipExpander;
        this.polarisServerUrl = polarisServerUrl;
        installDirectory = new File(downloadTargetDirectory, PolarisDownloadUtility.POLARIS_CLI_INSTALL_DIRECTORY);

        installDirectory.mkdirs();
        if (!installDirectory.exists() || !installDirectory.isDirectory() || !installDirectory.canWrite()) {
            throw new IllegalArgumentException("The provided directory must exist and be writable.");
        }
    }

    /**
     * The Polaris CLI will be download if it has not previously been downloaded or
     * if it has been updated on the server. The absolute path to the swip_cli
     * executable will be returned if it was downloaded or found successfully,
     * otherwise an Optional.empty will be returned and the log will contain
     * details concerning the failure.
     */
    public Optional<String> retrievePolarisCliExecutablePath() {
        File versionFile = null;
        try {
            versionFile = retrieveVersionFile();
        } catch (IOException e) {
            logger.error("Could not create the version file: " + e.getMessage());
            return Optional.empty();
        }

        String downloadUrl = getDownloadUrl();
        return retrievePolarisCliExecutablePath(versionFile, downloadUrl);
    }

    public Optional<String> retrievePolarisCliExecutablePath(File versionFile, String downloadUrl) {
        File binDirectory = null;
        try {
            binDirectory = downloadIfModified(versionFile, downloadUrl);
        } catch (Exception e) {
            logger.error("The Polaris CLI could not be downloaded successfully: " + e.getMessage());
        }

        if (binDirectory != null && binDirectory.exists() && binDirectory.isDirectory()) {
            try {
                File polarisCliExecutable = getPolarisCli(binDirectory);
                logger.info("Polaris CLI downloaded/found successfully: " + polarisCliExecutable.getCanonicalPath());
                return Optional.of(polarisCliExecutable.getCanonicalPath());
            } catch (Exception e) {
                logger.error("The Polaris CLI executable could not be found: " + e.getMessage());
            }
        }

        return Optional.empty();
    }

    public File retrieveVersionFile() throws IOException {
        File versionFile = new File(installDirectory, PolarisDownloadUtility.VERSION_FILENAME);
        if (!versionFile.exists()) {
            logger.info("The version file has not been created yet so creating it now.");
            versionFile.createNewFile();
            versionFile.setLastModified(0L);
        }

        return versionFile;
    }

    public String getDownloadUrl() {
        if (SystemUtils.IS_OS_MAC) {
            return polarisServerUrl + PolarisDownloadUtility.MAC_DOWNLOAD_URL;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return polarisServerUrl + PolarisDownloadUtility.WINDOWS_DOWNLOAD_URL;
        } else {
            return polarisServerUrl + PolarisDownloadUtility.LINUX_DOWNLOAD_URL;
        }
    }

    private File downloadIfModified(File versionFile, String downloadUrl) throws IOException, IntegrationException, ArchiveException {
        long lastTimeDownloaded = versionFile.lastModified();
        logger.debug(String.format("last time downloaded: %d", lastTimeDownloaded));

        Request downloadRequest = new Request.Builder(downloadUrl).build();
        try (Response response = intHttpClient.execute(downloadRequest)) {
            long lastModifiedOnServer = response.getLastModified();
            if (lastModifiedOnServer == lastTimeDownloaded) {
                logger.debug("The Polaris CLI has not been modified since it was last downloaded - skipping download.");
                return getBinDirectory();
            } else {
                logger.info("Downloading the Polaris CLI.");
                try (InputStream responseStream = response.getContent()) {
                    cleanupZipExpander.expand(responseStream, installDirectory);
                }
                versionFile.setLastModified(lastModifiedOnServer);

                File binDirectory = getBinDirectory();
                makeBinFilesExecutable(binDirectory);

                logger.info(String.format("Polaris CLI downloaded successfully."));

                return binDirectory;
            }
        }
    }

    // since we know that we only allow a single directory in installDirectory,
    // that single directory IS the expanded archive
    private File getBinDirectory() throws IntegrationException {
        File[] directories = installDirectory.listFiles(file -> file.isDirectory());
        if (directories == null || directories.length != 1) {
            throw new IntegrationException(String.format("The %s directory should only be modified by polaris-common. Please delete all files from that directory and try again.", PolarisDownloadUtility.POLARIS_CLI_INSTALL_DIRECTORY));
        }

        File polarisCliDirectory = directories[0];
        File bin = new File(polarisCliDirectory, "bin");

        return bin;
    }

    private void makeBinFilesExecutable(File binDirectory) {
        Arrays.stream(binDirectory.listFiles()).forEach(file -> {
            file.setExecutable(true);
        });
    }

    private File getPolarisCli(File binDirectory) throws IntegrationException {
        String polarisCliFilename = "swip_cli";
        if (SystemUtils.IS_OS_WINDOWS) {
            polarisCliFilename += ".exe";
        }

        File polarisCli = new File(binDirectory, polarisCliFilename);

        if (!polarisCli.exists() || !polarisCli.isFile() || !(polarisCli.length() > 0L)) {
            throw new IntegrationException("The polaris_cli does not appear to have been downloaded correctly - be sure to download it first.");
        }

        return polarisCli;
    }

}
