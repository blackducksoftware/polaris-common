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
package com.synopsys.integration.polaris.common.service;

import com.google.gson.Gson;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

public class PolarisServicesFactory {
    private final IntLogger logger;
    private final AccessTokenPolarisHttpClient httpClient;
    private final Gson gson;

    public PolarisServicesFactory(final IntLogger logger, final AccessTokenPolarisHttpClient httpClient, final Gson gson) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public PolarisService createPolarisService() {
        return new PolarisService(httpClient, gson);
    }

    public ProjectService createProjectService() {
        return new ProjectService(httpClient, createPolarisService());
    }

    public BranchService createBranchService() {
        return new BranchService(httpClient, createPolarisService());
    }

    public IssueService createIssueService() {
        return new IssueService(httpClient, createPolarisService());
    }

    public UserService createUserService() {
        return new UserService(httpClient, createPolarisService());
    }

}
