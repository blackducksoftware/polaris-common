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
package com.synopsys.integration.polaris.common.rest;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.client.AuthenticatingIntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class AccessTokenPolarisHttpClient extends AuthenticatingIntHttpClient {
    private static final String AUTHENTICATION_SPEC = "api/auth/authenticate";
    private static final String AUTHENTICATION_RESPONSE_KEY = "jwt";

    private static final String ACCESS_TOKEN_REQUEST_KEY = "accesstoken";
    private static final String ACCESS_TOKEN_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final Gson gson;
    private final AuthenticationSupport authenticationSupport;
    private final String baseUrl;
    private final String accessToken;

    public AccessTokenPolarisHttpClient(
            IntLogger logger, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, String baseUrl, String accessToken, Gson gson, AuthenticationSupport authenticationSupport) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.gson = gson;
        this.authenticationSupport = authenticationSupport;

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("No access token was found.");
        }
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        super.handleErrorResponse(request, response);

        authenticationSupport.handleTokenErrorResponse(this, request, response);
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return authenticationSupport.isTokenAlreadyAuthenticated(request);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        authenticationSupport.completeTokenAuthenticationRequest(request, response, logger, gson, this, AccessTokenPolarisHttpClient.AUTHENTICATION_RESPONSE_KEY);
    }

    @Override
    public final Response attemptAuthentication() throws IntegrationException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", AccessTokenPolarisHttpClient.ACCESS_TOKEN_REQUEST_CONTENT_TYPE);

        String httpBody = String.format("%s=%s", AccessTokenPolarisHttpClient.ACCESS_TOKEN_REQUEST_KEY, accessToken);
        HttpEntity httpEntity = new StringEntity(httpBody, StandardCharsets.UTF_8);

        RequestBuilder requestBuilder = createRequestBuilder(HttpMethod.POST, headers);
        requestBuilder.setEntity(httpEntity);

        return authenticationSupport.attemptAuthentication(this, baseUrl, AccessTokenPolarisHttpClient.AUTHENTICATION_SPEC, requestBuilder);
    }

}
