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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisResource;
import com.synopsys.integration.polaris.common.api.PolarisResourcesSingle;
import com.synopsys.integration.polaris.common.api.auth.PolarisRelationshipLinks;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.request.PolarisRequestSpec;
import com.synopsys.integration.polaris.common.request.param.PolarisParamBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;

public class AuthService {
    private static final String AUTH_API_SPEC_STRING = "/api/auth";
    private static final String USERS_API_SPEC_STRING = AUTH_API_SPEC_STRING + "/users";
    private static final String ROLE_ASSIGNMENTS_API_SPEC_STRING = AUTH_API_SPEC_STRING + "/role-assignments";

    public static final PolarisRequestSpec USERS_API_SPEC = PolarisRequestSpec.of(USERS_API_SPEC_STRING);
    public static final PolarisRequestSpec ROLE_ASSIGNMENTS_API_SPEC = PolarisRequestSpec.of(ROLE_ASSIGNMENTS_API_SPEC_STRING);

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public AuthService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public <R extends PolarisResource> List<R> getAll(final PolarisRequestSpec polarisRequestSpec, final Type resourcesType) throws IntegrationException {
        return getFiltered(polarisRequestSpec, (PolarisParamBuilder) null, resourcesType);
    }

    public <R extends PolarisResource> List<R> getFiltered(final PolarisRequestSpec polarisRequestSpec, final PolarisParamBuilder paramBuilder, final Type resourcesType) throws IntegrationException {
        final List<PolarisParamBuilder> paramBuilders = paramBuilder == null ? Arrays.asList() : Arrays.asList(paramBuilder);
        return getFiltered(polarisRequestSpec, paramBuilders, resourcesType);
    }

    public <R extends PolarisResource> List<R> getFiltered(final PolarisRequestSpec polarisRequestSpec, final Collection<PolarisParamBuilder> paramBuilders, final Type resourcesType) throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + polarisRequestSpec.getSpec();
        final Request.Builder pagedRequestBuilder = PolarisRequestFactory.createDefaultRequestBuilder().uri(uri);
        if (paramBuilders != null) {
            addParams(pagedRequestBuilder, paramBuilders);
        }

        final PolarisPagedRequestCreator createPagedRequest = (limit, offset) -> PolarisRequestFactory.populatePagedRequestBuilder(pagedRequestBuilder, limit, offset).build();
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(createPagedRequest, resourcesType);
        return polarisService.getAllResponses(pagedRequestWrapper);
    }

    public <R extends PolarisResource, T> Optional<T> getAttributeFromRelationship(final PolarisRelationshipLinks relationshipLinks, final Function<R, T> extractAttribute, final Type resourcesType)
        throws IntegrationException {
        final String uri = relationshipLinks.getRelated();
        final Request resourceRequest = PolarisRequestFactory.createDefaultPolarisGetRequest(uri);
        final PolarisResourcesSingle<R> response = polarisService.get(resourcesType, resourceRequest);

        return response.getData().map(extractAttribute::apply);
    }

    private void addParams(final Request.Builder requestBuilder, final Collection<PolarisParamBuilder> paramBuilders) {
        for (final PolarisParamBuilder paramBuilder : paramBuilders) {
            if (paramBuilder != null) {
                final Map.Entry<String, String> params = paramBuilder.build();
                requestBuilder.addQueryParameter(params.getKey(), params.getValue());
            }
        }
    }

}
