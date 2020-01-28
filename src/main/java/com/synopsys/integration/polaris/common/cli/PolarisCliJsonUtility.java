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
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.cli.model.PolarisCliScanModel;

public class PolarisCliJsonUtility {
    private Gson gson;

    public PolarisCliJsonUtility(final Gson gson) {
        this.gson = gson;
    }

    public static PolarisCliJsonUtility defaultUtility() {
        return new PolarisCliJsonUtility(new Gson());
    }

    public PolarisCliScanModel getPolarisCliScanModelFromDefaultLocation(final String projectRootDirectory) throws IntegrationException {
        final Path pathToJson = Paths.get(projectRootDirectory)
                                    .resolve(".synopsys")
                                    .resolve("polaris")
                                    .resolve("cli-scan.json");
        return getPolarisCliScanModelFromJson(pathToJson);
    }

    public PolarisCliScanModel getPolarisCliScanModelFromJson(final String pathToJson) throws IntegrationException {
        final Path actualPathToJson = Paths.get(pathToJson);
        return getPolarisCliScanModelFromJson(actualPathToJson);
    }

    public PolarisCliScanModel getPolarisCliScanModelFromJson(final Path pathToJson) throws IntegrationException {
        try {
            return gson.fromJson(Files.newBufferedReader(pathToJson), PolarisCliScanModel.class);
        } catch (final IOException e) {
            throw new IntegrationException("There was a problem parsing Polaris CLI response json at " + pathToJson.toString(), e);
        }
    }

}
