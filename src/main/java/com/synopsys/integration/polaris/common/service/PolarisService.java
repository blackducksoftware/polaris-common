/**
 * polaris-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
import com.synopsys.integration.polaris.common.api.PolarisResource;
import com.synopsys.integration.polaris.common.api.PolarisResourceSparse;
import com.synopsys.integration.polaris.common.api.PolarisResources;
import com.synopsys.integration.polaris.common.api.PolarisResourcesPagination;
import com.synopsys.integration.polaris.common.api.PolarisResponse;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class PolarisService {
    public static final String PROJECT_ID = "project-id";
    public static final String BRANCH_ID = "branch-id";

    public static final String COMMON_API_SPEC = "/api/common/v0";
    public static final String PROJECT_API_SPEC = COMMON_API_SPEC + "/projects";
    public static final String BRANCHES_API_SPEC = COMMON_API_SPEC + "/branches";

    public static final String QUERY_API_SPEC = "/api/query/v0";
    public static final String ISSUES_API_SPEC = QUERY_API_SPEC + "/issues";

    public static final String GET_ISSUE_API_SPEC(final String issueKey) {
        return ISSUES_API_SPEC + "/" + issueKey;
    }

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisJsonTransformer polarisJsonTransformer;
    private final int defaultPageSize;

    public PolarisService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisJsonTransformer polarisJsonTransformer, final int defaultPageSize) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisJsonTransformer = polarisJsonTransformer;
        this.defaultPageSize = defaultPageSize;
    }

    public <R extends PolarisResource> Optional<R> getResourceFromPopulated(final PolarisResponse populatedResources, final PolarisResourceSparse sparseResourceData, final Class<R> resourceClass) {
        final String id = StringUtils.defaultString(sparseResourceData.getId());
        final String type = StringUtils.defaultString(sparseResourceData.getType());
        for (final PolarisResource includedResource : populatedResources.getIncluded()) {
            if (type.equals(includedResource.getType()) && id.equals(includedResource.getId())) {
                try {
                    final R fullyTypedResource = polarisJsonTransformer.getResponseAs(includedResource.getJson(), resourceClass);
                    return Optional.of(fullyTypedResource);
                } catch (final IntegrationException e) {
                    break;
                }
            }
        }
        return Optional.empty();
    }

    public <R extends PolarisComponent> R get(final Type returnType, final Request request) throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();

            return polarisJsonTransformer.getResponse(response, returnType);
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public <R extends PolarisResource> Optional<R> getFirstResponse(final Request request, final Type resourcesType) throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();
            final PolarisResources<R> wrappedResponse = polarisJsonTransformer.getResponse(response, resourcesType);
            if (wrappedResponse != null) {
                final List<R> data = wrappedResponse.getData();
                if (null != data && !data.isEmpty()) {
                    return Optional.ofNullable(data.get(0));
                }
            }
            return Optional.empty();
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public <R extends PolarisResource> List<R> getAllResponses(final PolarisPagedRequestWrapper polarisPagedRequestWrapper) throws IntegrationException {
        return getAllResponses(polarisPagedRequestWrapper, defaultPageSize);
    }

    public <R extends PolarisResource, W extends PolarisResources<R>> List<R> getAllResponses(final PolarisPagedRequestWrapper polarisPagedRequestWrapper, final int pageSize) throws IntegrationException {
        final W populatedResponse = getPopulatedResponse(polarisPagedRequestWrapper, pageSize);
        return populatedResponse.getData();
    }

    public <R extends PolarisResource, W extends PolarisResources<R>> W getPopulatedResponse(final PolarisPagedRequestWrapper polarisPagedRequestWrapper) throws IntegrationException {
        return getPopulatedResponse(polarisPagedRequestWrapper, defaultPageSize);
    }

    public <R extends PolarisResource, W extends PolarisResources<R>> W getPopulatedResponse(final PolarisPagedRequestWrapper polarisPagedRequestWrapper, final int pageSize) throws IntegrationException {
        W populatedResources = null;
        final Set<R> allData = new HashSet<>();
        final Set<PolarisResourceSparse> allIncluded = new HashSet<>();

        int totalExpected = 0;
        int offset = 0;
        do {
            final W wrappedResponse = executePagedRequest(polarisPagedRequestWrapper, offset, pageSize);
            if (null == populatedResources) {
                populatedResources = wrappedResponse;
            }
            if (wrappedResponse != null) {
                final PolarisResourcesPagination meta = wrappedResponse.getMeta();
                totalExpected = meta.getTotal().intValue();

                final List<R> data = wrappedResponse.getData();
                allData.addAll(data);

                final List<PolarisResourceSparse> included = wrappedResponse.getIncluded();
                allIncluded.addAll(included);

                offset += pageSize;
            }
        } while (totalExpected > allData.size());

        populatedResources.setData(new ArrayList<>(allData));
        populatedResources.setIncluded(new ArrayList<>(allIncluded));
        return populatedResources;
    }

    private <R extends PolarisResource, W extends PolarisResources<R>> W executePagedRequest(final PolarisPagedRequestWrapper polarisPagedRequestWrapper, final int offset, final int limit) throws IntegrationException {
        final Request pagedRequest = polarisPagedRequestWrapper.getRequestCreator().apply(limit, offset);
        try (final Response response = polarisHttpClient.execute(pagedRequest)) {
            response.throwExceptionForError();
            return polarisJsonTransformer.getResponse(response, polarisPagedRequestWrapper.getResponseType());
        } catch (final IOException e) {
            throw new IntegrationException("Problem handling request", e);
        }
    }

}
