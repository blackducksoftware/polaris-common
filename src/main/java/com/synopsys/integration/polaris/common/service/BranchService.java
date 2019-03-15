package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0Resources;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class BranchService {
    private static final PolarisContainerResponseExtractor<BranchV0Resources, BranchV0> BRANCH_EXTRACTOR = new PolarisContainerResponseExtractor<>(BranchV0Resources::getData, BranchV0Resources::getMeta);

    private AccessTokenPolarisHttpClient polarisHttpClient;
    private PolarisService polarisService;

    public BranchService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<BranchV0> getBranchesForProject(final String projectId) throws IntegrationException {
        BiFunction<Integer, Integer, Request> createPagedRequest = (limit, offset) -> createBranchesGetRequest(limit, offset, projectId);
        PolarisPagedRequestCreator<BranchV0Resources, BranchV0> branchPagedRequestCreator = new PolarisPagedRequestCreator<>(createPagedRequest, BRANCH_EXTRACTOR);

        return polarisService.getAllResponses(BranchV0Resources.class, branchPagedRequestCreator);
    }

    public Optional<BranchV0> getBranchForProjectByName(String projectId, String branchName) throws IntegrationException {
        Request.Builder requestBuilder = createBranchForProjectIdRequestBuilder(projectId);
        requestBuilder.addQueryParameter(PolarisService.FILTER_BRANCH_NAME_CONTAINS, branchName);
        Request request = requestBuilder.build();

        return polarisService.getFirstResponse(BranchV0Resources.class, request, BRANCH_EXTRACTOR);
    }

    public Request createBranchesGetRequest(int limit, int offset, final String projectId) {
        Request.Builder requestBuilder = createBranchForProjectIdRequestBuilder(projectId);
        PolarisRequestFactory.populatePagedRequestBuilder(requestBuilder, limit, offset);
        return requestBuilder.build();
    }

    private Request.Builder createBranchForProjectIdRequestBuilder(String projectId) {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.BRANCHES_API_SPEC;
        return PolarisRequestFactory.createDefaultRequestBuilder()
                       .addQueryParameter(PolarisService.FILTER_BRANCH_PROJECT_ID_EQUALS, projectId)
                       .uri(uri);
    }

}
