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
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.model.user.EmailDetails;
import com.synopsys.integration.polaris.common.model.user.EmailDetailsResource;
import com.synopsys.integration.polaris.common.model.user.UserModel;
import com.synopsys.integration.polaris.common.model.user.UserResourcesModel;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class UserService {
    public static final String EMAIL_DETAILS_LINK = "/email-details";
    private static final PolarisContainerResponseExtractor<UserResourcesModel, UserModel> USER_EXTRACTOR = new PolarisContainerResponseExtractor<>(UserResourcesModel::getData, UserResourcesModel::getMeta);

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public UserService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public List<UserModel> getAllUsers() throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + PolarisService.USERS_API_SPEC;
        final BiFunction<Integer, Integer, Request> createPagedRequest = (limit, offset) -> PolarisRequestFactory.createCommonPolarisGetRequest(uri, offset, limit);
        final PolarisPagedRequestCreator<UserResourcesModel, UserModel> issuePagedRequestCreator = new PolarisPagedRequestCreator<>(createPagedRequest, USER_EXTRACTOR);

        return polarisService.getAllResponses(UserResourcesModel.class, issuePagedRequestCreator);
    }

    public Optional<String> getEmailForUser(final UserModel user) throws IntegrationException {
        String email = user.getAttributes().getEmail();
        if (StringUtils.isBlank(email)) {
            final String uri = String.format("%s%s/%s%s", polarisHttpClient.getPolarisServerUrl(), polarisService.USERS_API_SPEC, "/", user.getId(), EMAIL_DETAILS_LINK);
            final Request usersRequest = PolarisRequestFactory.createDefaultPolarisGetRequest(uri);

            final EmailDetailsResource emailDetailsResource = polarisService.get(EmailDetailsResource.class, usersRequest);
            email = emailDetailsResource.getData().getAttributes().getEmail();
        }

        return Optional.ofNullable(email);
    }

}
