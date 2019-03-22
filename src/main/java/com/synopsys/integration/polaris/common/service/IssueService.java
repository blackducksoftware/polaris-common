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

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.model.Issue;
import com.synopsys.integration.polaris.common.model.QueryIssue;
import com.synopsys.integration.polaris.common.model.QueryIssues;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class IssueService {
    private static final TypeToken ISSUE_RESOURCES = new TypeToken<QueryIssues>() {};

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public IssueService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<QueryIssue> getIssuesForProjectAndBranch(final String projectId, final String branchId) throws IntegrationException {
        final PolarisPagedRequestCreator createPagedRequest = (limit, offset) -> createIssuesGetRequest(limit, offset, projectId, branchId);
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(createPagedRequest, ISSUE_RESOURCES.getType());
        return polarisService.getAllResponses(pagedRequestWrapper);
    }

    public Issue getIssueForProjectBranchAndIssueKey(final String projectId, final String branchId, final String issueKey) throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.GET_ISSUE_API_SPEC(issueKey);
        final Request.Builder requestBuilder = createRequestBuilder(uri, projectId, branchId);
        final Request request = requestBuilder.build();
        return polarisService.get(Issue.class, request);
    }

    public Request createIssuesGetRequest(final int limit, final int offset, final String projectId, final String branchId) {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.ISSUES_API_SPEC;
        final Request.Builder requestBuilder = createRequestBuilder(uri, projectId, branchId);
        PolarisRequestFactory.populatePagedRequestBuilder(requestBuilder, limit, offset);
        return requestBuilder.build();
    }

    private Request.Builder createRequestBuilder(final String uri, final String projectId, final String branchId) {
        return PolarisRequestFactory.createDefaultRequestBuilder()
                   .addQueryParameter(PolarisService.PROJECT_ID, projectId)
                   .addQueryParameter(PolarisService.BRANCH_ID, branchId)
                   .uri(uri);
    }

}
