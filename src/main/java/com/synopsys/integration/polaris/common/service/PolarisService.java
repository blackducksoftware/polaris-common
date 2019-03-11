package com.synopsys.integration.polaris.common.service;

import java.io.IOException;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.util.Stringable;

public class PolarisService {
    public static String DEFAULT_MIME_TYPE = "application/vnd.api+json";

    private final IntLogger logger;
    private final AccessTokenPolarisHttpClient httpClient;
    private final Gson gson;

    public PolarisService(final IntLogger logger, final AccessTokenPolarisHttpClient httpClient, final Gson gson) {
        this.logger = logger;
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public Request createDefaultPolarisGetRequest(final String requestUri) {
        return createCommonPolarisGetRequest(requestUri, 10);
    }

    public Request createCommonPolarisGetRequest(final String requestUri, final Integer pageLimit) {
        return createCommonPolarisGetRequest(requestUri, 0, pageLimit);
    }

    public Request createCommonPolarisGetRequest(final String requestUri, final Integer pageOffset, final Integer pageLimit) {
        return new Request.Builder().uri(requestUri).addQueryParameter("page[offset]", pageOffset.toString()).addQueryParameter("page[limit]", pageLimit.toString()).mimeType(DEFAULT_MIME_TYPE).method(HttpMethod.GET).build();
    }

    public <T extends Stringable> T executeGetRequest(final Request request, final Class<T> clazz) throws IntegrationException {
        try (final Response response = httpClient.execute(request)) {
            response.throwExceptionForError();
            final String content = response.getContentString();
            return gson.fromJson(content, clazz);
        } catch (final IOException e) {
            throw new IntegrationException(e);
        }
    }
}
