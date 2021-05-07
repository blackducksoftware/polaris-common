/*
 * polaris-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisRelationshipSingle;
import com.synopsys.integration.polaris.common.api.PolarisResourceSparse;
import com.synopsys.integration.polaris.common.api.query.model.issue.IssueV0Relationships;
import com.synopsys.integration.polaris.common.api.query.model.issue.IssueV0Resource;
import com.synopsys.integration.polaris.common.api.query.model.issue.IssueV0Resources;
import com.synopsys.integration.polaris.common.api.query.model.issue.type.IssueTypeV0Resource;
import com.synopsys.integration.polaris.common.model.IssueResourcesSingle;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class IssueService {
    private static final TypeToken ISSUE_RESOURCES = new TypeToken<IssueV0Resources>() {};
    private static final TypeToken SINGLE_ISSUE_RESOURCES = new TypeToken<IssueResourcesSingle>() {};

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public IssueService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<IssueV0Resource> getIssuesForProjectAndBranch(final String projectId, final String branchId) throws IntegrationException {
        final PolarisPagedRequestCreator createPagedRequest = (limit, offset) -> createIssuesGetRequest(limit, offset, projectId, branchId);
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(createPagedRequest, ISSUE_RESOURCES.getType());
        return polarisService.getAllResponses(pagedRequestWrapper);
    }

    public IssueResourcesSingle getIssueForProjectBranchAndIssueKeyWithDefaultIncluded(final String projectId, final String branchId, final String issueKey) throws IntegrationException {
        final HttpUrl url = polarisHttpClient.appendToPolarisUrl(PolarisService.GET_ISSUE_API_SPEC(issueKey));
        final Request.Builder requestBuilder = createRequestBuilder(url, projectId, branchId);
        final Request request = requestBuilder.build();
        return polarisService.get(SINGLE_ISSUE_RESOURCES.getType(), request);
    }

    public Optional<IssueTypeV0Resource> getIssueTypeFromPopulatedIssueResources(final IssueResourcesSingle issueResources) {
        final Optional<PolarisResourceSparse> optionalIssueTypeData = issueResources.getData()
                                                                          .map(IssueV0Resource::getRelationships)
                                                                          .map(IssueV0Relationships::getIssueType)
                                                                          .flatMap(PolarisRelationshipSingle::getData);
        if (optionalIssueTypeData.isPresent()) {
            return polarisService.getResourceFromPopulated(issueResources, optionalIssueTypeData.get(), IssueTypeV0Resource.class);
        }
        return Optional.empty();
    }

    public Request createIssuesGetRequest(final int limit, final int offset, final String projectId, final String branchId) {
        final HttpUrl url = polarisHttpClient.appendToPolarisUrl(PolarisService.ISSUES_API_SPEC);
        final Request.Builder requestBuilder = createRequestBuilder(url, projectId, branchId);
        PolarisRequestFactory.populatePagedRequestBuilder(requestBuilder, limit, offset);
        return requestBuilder.build();
    }

    private Request.Builder createRequestBuilder(final HttpUrl url, final String projectId, final String branchId) {
        return PolarisRequestFactory.createDefaultRequestBuilder()
                   .addQueryParameter(PolarisService.PROJECT_ID, projectId)
                   .addQueryParameter(PolarisService.BRANCH_ID, branchId)
                   .url(url);
    }

}
