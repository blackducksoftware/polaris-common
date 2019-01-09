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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.connection.ReconnectingRestConnection;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Response;

public class AccessTokenRestConnection extends ReconnectingRestConnection {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHENTICATION_SPEC = "api/auth/authenticate";
    private static final String AUTHENTICATION_RESPONSE_KEY = "jwt";

    private static final String ACCESS_TOKEN_REQUEST_KEY = "accesstoken";
    private static final String ACCESS_TOKEN_REQUEST_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final String baseUrl;
    private final String accessToken;

    public AccessTokenRestConnection(final IntLogger logger, final int timeout, final boolean alwaysTrustServerCertificate, final ProxyInfo proxyInfo, final String baseUrl, final String accessToken) {
        super(logger, timeout, alwaysTrustServerCertificate, proxyInfo);
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("No access token was found.");
        }
    }

    @Override
    public void handleErrorResponse(final HttpUriRequest request, final Response response) {
        super.handleErrorResponse(request, response);

        if (isUnauthorized(response.getStatusCode()) && request.containsHeader(AUTHORIZATION_HEADER)) {
            request.removeHeaders(AUTHORIZATION_HEADER);
            removeCommonRequestHeader(AUTHORIZATION_HEADER);
        }
    }

    /**
     * Gets the cookie for the Authorized connection to the Black Duck server. Returns the response code from the connection.
     */
    @Override
    public void finalizeRequest(final HttpUriRequest request) {
        super.finalizeRequest(request);

        if (request.containsHeader(AUTHORIZATION_HEADER)) {
            // Already authenticated
            return;
        }

        final Optional<String> bearerToken = retrieveBearerToken();
        if (bearerToken.isPresent()) {
            final String headerValue = "Bearer " + bearerToken.get();
            addCommonRequestHeader(AUTHORIZATION_HEADER, headerValue);
            request.addHeader(AUTHORIZATION_HEADER, headerValue);
        } else {
            getLogger().error("No Bearer token found when authenticating");
        }
    }

    public final Response attemptAuthentication() throws IntegrationException, IOException {
        final URL authenticationUrl;
        try {
            authenticationUrl = new URL(getBaseUrl(), AUTHENTICATION_SPEC);
        } catch (final MalformedURLException e) {
            throw new IntegrationException("Error constructing the authentication URL: " + e.getMessage(), e);
        }

        final RequestBuilder requestBuilder = createRequestBuilder(HttpMethod.POST, getRequestHeaders());
        requestBuilder.setCharset(Charsets.UTF_8);
        requestBuilder.setUri(authenticationUrl.toString());
        requestBuilder.setEntity(getEntity());
        final HttpUriRequest request = requestBuilder.build();
        logRequestHeaders(request);

        final CloseableHttpClient closeableHttpClient = getClientBuilder().build();
        final CloseableHttpResponse closeableHttpResponse;
        closeableHttpResponse = closeableHttpClient.execute(request);
        logResponseHeaders(closeableHttpResponse);
        return new Response(request, closeableHttpClient, closeableHttpResponse);
    }

    public URL getBaseUrl() {
        try {
            return new URL(baseUrl);
        } catch (final MalformedURLException e) {
            return null;
        }
    }

    private Optional<String> retrieveBearerToken() {
        try (final Response response = attemptAuthentication()) {
            if (response.isStatusCodeOkay()) {
                final String bearerToken = readBearerToken(response.getActualResponse());
                return Optional.of(bearerToken);
            }
        } catch (final IntegrationException | IOException e) {
            logger.error("Could not retrieve the bearer token", e);
        }
        return Optional.empty();
    }

    private Map<String, String> getRequestHeaders() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", ACCESS_TOKEN_REQUEST_CONTENT_TYPE);

        return headers;
    }

    private StringEntity getEntity() throws IntegrationException {
        try {
            return new StringEntity(String.format("%s=%s", ACCESS_TOKEN_REQUEST_KEY, accessToken));
        } catch (final UnsupportedEncodingException e) {
            throw new IntegrationException(e);
        }
    }

    private String readBearerToken(final CloseableHttpResponse response) throws IOException {
        final JsonParser jsonParser = new JsonParser();
        final String bodyToken;
        try (final InputStream inputStream = response.getEntity().getContent()) {
            bodyToken = IOUtils.toString(inputStream, Charsets.UTF_8);
        }
        final JsonObject bearerResponse = jsonParser.parse(bodyToken).getAsJsonObject();
        return bearerResponse.get(AUTHENTICATION_RESPONSE_KEY).getAsString();
    }

}
