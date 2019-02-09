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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class PolarisEnvironmentCheck {
    public static final String POLARIS_HOME_ENVIRONMENT_VARIABLE = "SWIP_HOME";
    public static final String POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE = "SWIP_ACCESS_TOKEN_FILE";
    public static final String POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE = "SWIP_ACCESS_TOKEN";

    public static final String POLARIS_CONFIG_DIRECTORY_DEFAULT = ".swip";
    public static final String POLARIS_ACCESS_TOKEN_FILENAME_DEFAULT = ".access_token";

    private final PolarisEnvironment polarisEnvironment;

    public static PolarisEnvironmentCheck withSystemDefaults() {
        return new PolarisEnvironmentCheck(SystemPolarisEnvironment.withSystemDefaults());
    }

    public PolarisEnvironmentCheck(PolarisEnvironment polarisEnvironment) {
        this.polarisEnvironment = polarisEnvironment;
    }

    public boolean isAccessTokenConfigured() {
        try {
            Optional<String> accessToken = getPolarisAccessToken();
            return accessToken.isPresent();
        } catch (IntegrationException ignored) {
            // ignored
        }
        return false;
    }

    public Optional<String> getPolarisAccessToken() throws IntegrationException {
        if (polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE)) {
            String accessToken = polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE);
            if (StringUtils.isNotBlank(accessToken)) {
                return Optional.of(accessToken);
            }
        }

        File polarisConfigDirectory = getPolarisConfigDirectory();
        File accessTokenFile = getPolarisAccessTokenFile(polarisConfigDirectory);
        if (accessTokenFile.length() < 100) {
            try {
                byte[] accessTokenBytes = Files.readAllBytes(accessTokenFile.toPath());
                String accessToken = new String(accessTokenBytes, StandardCharsets.UTF_8);
                if (StringUtils.isNotBlank(accessToken)) {
                    return Optional.of(accessToken.trim());
                }
            } catch (IOException e) {
                throw new IntegrationException(String.format("Could not read the access token file %s: %s", accessTokenFile.getAbsolutePath(), e.getMessage()), e);
            }
        }

        return Optional.empty();
    }

    private File getPolarisConfigDirectory() {
        if (polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_HOME_ENVIRONMENT_VARIABLE)) {
            return new File(polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_HOME_ENVIRONMENT_VARIABLE), PolarisEnvironmentCheck.POLARIS_CONFIG_DIRECTORY_DEFAULT);
        } else {
            File userHomeDirectory = polarisEnvironment.getUserHome();
            if (null != userHomeDirectory && userHomeDirectory.exists() && userHomeDirectory.isDirectory()) {
                return new File(userHomeDirectory, PolarisEnvironmentCheck.POLARIS_CONFIG_DIRECTORY_DEFAULT);
            } else {
                return null;
            }
        }
    }

    private File getPolarisAccessTokenFile(File polarisConfigDirectory) {
        String accessTokenFilename = PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILENAME_DEFAULT;
        if (polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE)) {
            accessTokenFilename = polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE);
        }

        File accessTokenFile = new File(polarisConfigDirectory, accessTokenFilename);
        return accessTokenFile;
    }

}
