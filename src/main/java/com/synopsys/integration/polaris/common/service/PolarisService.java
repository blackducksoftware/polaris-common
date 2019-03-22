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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
import com.synopsys.integration.polaris.common.api.PolarisResource;
import com.synopsys.integration.polaris.common.api.PolarisResources;
import com.synopsys.integration.polaris.common.api.PolarisResourcesPagination;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
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
    private final Gson gson;

    public PolarisService(final AccessTokenPolarisHttpClient polarisHttpClient, final Gson gson) {
        this.polarisHttpClient = polarisHttpClient;
        this.gson = gson;
    }

    public <R extends PolarisComponent> R get(final Type returnType, final Request request) throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();

            final String content = response.getContentString();
            final R returnObject = gson.fromJson(content, returnType);
            returnObject.setJson(content);

            return returnObject;
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }

    public <R extends PolarisResource> Optional<R> getFirstResponse(final Request request, final Type resourcesType) throws IntegrationException {
        try (final Response response = polarisHttpClient.execute(request)) {
            response.throwExceptionForError();
            final PolarisResources wrappedResponse = gson.fromJson(response.getContentString(), resourcesType);
            if (wrappedResponse != null) {
                final List<R> data = (List<R>) wrappedResponse.getData();
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
        return getAllResponses(polarisPagedRequestWrapper, PolarisRequestFactory.DEFAULT_LIMIT);
    }

    public <R extends PolarisResource> List<R> getAllResponses(final PolarisPagedRequestWrapper polarisPagedRequestWrapper, final int pageSize) throws IntegrationException {
        final List<R> allResults = new ArrayList<>();

        int totalExpected = 0;
        final int limit = pageSize;
        int offset = 0;
        do {
            final Request pagedRequest = polarisPagedRequestWrapper.getRequestCreator().apply(limit, offset);
            try (final Response response = polarisHttpClient.execute(pagedRequest)) {
                response.throwExceptionForError();
                final PolarisResources wrappedResponse = gson.fromJson(response.getContentString(), polarisPagedRequestWrapper.getResponseType());

                if (wrappedResponse != null) {
                    final PolarisResourcesPagination meta = wrappedResponse.getMeta();
                    totalExpected = meta.getTotal().intValue();

                    final List<R> data = (List<R>) wrappedResponse.getData();
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
