/**
 * polaris-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.polaris.common.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliResponseModel;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;

public class PolarisCliResponseUtility {
    private final IntLogger logger;
    private final Gson gson;

    public PolarisCliResponseUtility(final IntLogger logger, final Gson gson) {
        this.logger = logger;
        this.gson = gson;
    }

    public static PolarisCliResponseUtility defaultUtility(final IntLogger logger) {
        return new PolarisCliResponseUtility(logger, new Gson());
    }

    public static Path getDefaultPathToJson(final String projectRootDirectory) {
        return Paths.get(projectRootDirectory)
                   .resolve(".synopsys")
                   .resolve("polaris")
                   .resolve("cli-scan.json");
    }

    public Gson getGson() {
        return gson;
    }

    public PolarisCliResponseModel getPolarisCliResponseModelFromDefaultLocation(final String projectRootDirectory) throws PolarisIntegrationException {
        final Path pathToJson = getDefaultPathToJson(projectRootDirectory);
        return getPolarisCliResponseModel(pathToJson);
    }

    public PolarisCliResponseModel getPolarisCliResponseModel(final String pathToJson) throws PolarisIntegrationException {
        final Path actualPathToJson = Paths.get(pathToJson);
        return getPolarisCliResponseModel(actualPathToJson);
    }

    public PolarisCliResponseModel getPolarisCliResponseModel(final Path pathToJson) throws PolarisIntegrationException {
        try {
            logger.debug("Attempting to retrieve PolarisCliResponseModel from " + pathToJson.toString());
            return gson.fromJson(Files.newBufferedReader(pathToJson), PolarisCliResponseModel.class);
        } catch (final IOException e) {
            throw new PolarisIntegrationException("There was a problem parsing the Polaris CLI response json at " + pathToJson.toString(), e);
        }
    }

    public PolarisCliResponseModel getPolarisCliResponseModelFromString(final String rawPolarisCliResponse) {
        return gson.fromJson(rawPolarisCliResponse, PolarisCliResponseModel.class);
    }

}
