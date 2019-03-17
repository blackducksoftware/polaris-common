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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
import com.synopsys.integration.polaris.common.api.generated.common.ResourcesPagination;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class PolarisService {
    public static final String FILTER_PROJECT_NAME_CONTAINS = "filter[project][name][$eq]";
    public static final String FILTER_BRANCH_NAME_CONTAINS = "filter[branch][name][$eq]";
    public static final String FILTER_BRANCH_PROJECT_ID_EQUALS = "filter[branch][project][id][$eq]";

    public static final String PROJECT_ID = "project-id";
    public static final String BRANCH_ID = "branch-id";

    public static final String COMMON_API_SPEC = "/api/common/v0";
    public static final String PROJECT_API_SPEC = COMMON_API_SPEC + "/projects";
    public static final String BRANCHES_API_SPEC = COMMON_API_SPEC + "/branches";

    public static final String QUERY_API_SPEC = "/api/query/v0";
    public static final String ISSUES_API_SPEC = QUERY_API_SPEC + "/issues";

    public static final String GET_ISSUE_API_SPEC(String issueKey) {
        return ISSUES_API_SPEC + "/" + issueKey;
    }

    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final Gson gson;

    public PolarisService(AccessTokenPolarisHttpClient polarisHttpClient, Gson gson) {
        this.polarisHttpClient = polarisHttpClient;
        this.gson = gson;
    }

    public <R extends PolarisComponent> R get(Class<R> returnClass, Request request) throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();

            final String content = response.getContentString();
            R returnObject = gson.fromJson(content, returnClass);
            returnObject.setJson(content);

            return returnObject;
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public <W extends PolarisComponent, R extends PolarisComponent> Optional<R> getFirstResponse(Class<W> wrapperClass, Request request, PolarisContainerResponseExtractor<W, R> polarisContainerResponseExtractor)
            throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();
            final W wrappedResponse = gson.fromJson(response.getContentString(), wrapperClass);
            if (wrappedResponse != null) {
                final List<R> data = polarisContainerResponseExtractor.getGetResponseList().apply(wrappedResponse);
                if (null != data && !data.isEmpty()) {
                    return Optional.ofNullable(data.get(0));
                }
            }
            return Optional.empty();
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public <W extends PolarisComponent, R extends PolarisComponent> List<R> getAllResponses(Class<W> wrapperClass, PolarisPagedRequestCreator<W, R> polarisPagedRequestCreator) throws IntegrationException {
        return getAllResponses(wrapperClass, polarisPagedRequestCreator, PolarisRequestFactory.DEFAULT_LIMIT);
    }

    public <W extends PolarisComponent, R extends PolarisComponent> List<R> getAllResponses(Class<W> wrapperClass, PolarisPagedRequestCreator<W, R> polarisPagedRequestCreator, int pageSize) throws IntegrationException {
        final List<R> allResults = new ArrayList<>();

        int totalExpected = 0;
        int limit = pageSize;
        int offset = 0;
        do {
            final Request pagedRequest = polarisPagedRequestCreator.getCreatePagedRequest().apply(limit, offset);
            try (final Response response = polarisHttpClient.execute(pagedRequest)) {
                response.throwExceptionForError();
                final W wrappedResponse = gson.fromJson(response.getContentString(), wrapperClass);

                if (wrappedResponse != null) {
                    PolarisContainerResponseExtractor<W, R> polarisContainerResponseExtractor = polarisPagedRequestCreator.getPolarisContainerResponseExtractor();
                    final ResourcesPagination meta = polarisContainerResponseExtractor.getGetMetaFunction().apply(wrappedResponse);
                    totalExpected = meta.getTotal().intValue();

                    final List<R> data = polarisContainerResponseExtractor.getGetResponseList().apply(wrappedResponse);
                    allResults.addAll(data);
                    offset += limit;
                }
            } catch (final IOException e) {
                throw new IntegrationException("Problem handling request", e);
            }
        } while (totalExpected > allResults.size());

        return allResults;
    }

}
