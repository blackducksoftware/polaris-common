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

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.generated.common.ProjectV0;
import com.synopsys.integration.polaris.common.api.generated.common.ProjectV0Resources;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class ProjectService {
    private static final PolarisContainerResponseExtractor<ProjectV0Resources, ProjectV0> PROJECT_EXTRACTOR = new PolarisContainerResponseExtractor<>(ProjectV0Resources::getData, ProjectV0Resources::getMeta);

    private AccessTokenPolarisHttpClient polarisHttpClient;
    private PolarisService polarisService;

    public ProjectService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public Optional<ProjectV0> getProjectByName(String projectName) throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.PROJECT_API_SPEC;
        Request request =
                PolarisRequestFactory.createDefaultRequestBuilder()
                        .addQueryParameter(PolarisService.FILTER_PROJECT_NAME_CONTAINS, projectName)
                        .uri(uri)
                        .build();
        return polarisService.getFirstResponse(ProjectV0Resources.class, request, PROJECT_EXTRACTOR);
    }

    public List<ProjectV0> getAllProjects() throws IntegrationException {
        PolarisPagedRequestCreator<ProjectV0Resources, ProjectV0> projectPagedRequestCreator = new PolarisPagedRequestCreator<>(this::createProjectGetRequest, PROJECT_EXTRACTOR);
        return polarisService.getAllResponses(ProjectV0Resources.class, projectPagedRequestCreator);
    }

    public Request createProjectGetRequest(int limit, int offset) {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.PROJECT_API_SPEC;
        return PolarisRequestFactory.createDefaultPagedRequestBuilder(limit, offset)
                       .uri(uri)
                       .build();
    }

}
