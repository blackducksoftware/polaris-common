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
