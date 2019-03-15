package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
import com.synopsys.integration.polaris.common.model.QueryIssue;
import com.synopsys.integration.polaris.common.model.QueryIssues;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class IssueService {
    private static final PolarisContainerResponseExtractor<QueryIssues, QueryIssue> ISSUE_EXTRACTOR = new PolarisContainerResponseExtractor<>(QueryIssues::getData, QueryIssues::getMeta);

    private AccessTokenPolarisHttpClient polarisHttpClient;
    private PolarisService polarisService;

    public IssueService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<QueryIssue> getIssuesForProjectAndBranch(String projectId, String branchId) throws IntegrationException {
        BiFunction<Integer, Integer, Request> createPagedRequest = (limit, offset) -> createIssuesGetRequest(limit, offset, projectId, branchId);
        PolarisPagedRequestCreator<QueryIssues, QueryIssue> issuePagedRequestCreator = new PolarisPagedRequestCreator<>(createPagedRequest, ISSUE_EXTRACTOR);

        return polarisService.getAllResponses(QueryIssues.class, issuePagedRequestCreator);
    }

    public PolarisComponent getIssueForProjectBranchAndIssueKey(String projectId, String branchId, String issueKey) throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.GET_ISSUE_API_SPEC(issueKey);
        Request.Builder requestBuilder = createRequestBuilder(uri, projectId, branchId);
        Request request = requestBuilder.build();
        return polarisService.get(PolarisComponent.class, request);
    }

    public Request createIssuesGetRequest(int limit, int offset, final String projectId, final String branchId) {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.ISSUES_API_SPEC;
        Request.Builder requestBuilder = createRequestBuilder(uri, projectId, branchId);
        PolarisRequestFactory.populatePagedRequestBuilder(requestBuilder, limit, offset);
        return requestBuilder.build();
    }

    private Request.Builder createRequestBuilder(String uri, String projectId, String branchId) {
        return PolarisRequestFactory.createDefaultRequestBuilder()
                       .addQueryParameter(PolarisService.PROJECT_ID, projectId)
                       .addQueryParameter(PolarisService.BRANCH_ID, branchId)
                       .uri(uri);
    }

}
