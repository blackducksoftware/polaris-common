package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.function.BiFunction;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.generated.auth.User;
import com.synopsys.integration.polaris.common.model.UserResourcesModel;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class UserService {
    public static final String EMAIL_DETAILS_LINK = "/email-details";
    private static final PolarisContainerResponseExtractor<UserResourcesModel, User> USER_EXTRACTOR = new PolarisContainerResponseExtractor<>(UserResourcesModel::getData, UserResourcesModel::getMeta);

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public UserService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<User> getAllUsers() throws IntegrationException {
        final BiFunction<Integer, Integer, Request> createPagedRequest = (limit, offset) -> createUsersGetRequest(limit, offset);
        final PolarisPagedRequestCreator<UserResourcesModel, User> issuePagedRequestCreator = new PolarisPagedRequestCreator<>(createPagedRequest, USER_EXTRACTOR);

        return polarisService.getAllResponses(UserResourcesModel.class, issuePagedRequestCreator);
    }

    public Request createUsersGetRequest(final int offset, final int limit) {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.USERS_API_SPEC;
        final Request.Builder requestBuilder = new Request.Builder(uri);
        PolarisRequestFactory.populatePagedRequestBuilder(requestBuilder, limit, offset);
        return requestBuilder.build();
    }

}
