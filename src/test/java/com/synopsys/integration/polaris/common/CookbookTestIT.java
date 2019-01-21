package com.synopsys.integration.polaris.common;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.ProjectV0Resources;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClientTestIT;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class CookbookTestIT {

    @Test
    public void getSomePolarisProjectsTest() throws IntegrationException, IOException {
        final String baseUrl = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_BASE_URL);
        final String accessToken = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_ACCESS_TOKEN);

        final Gson gson = new Gson();
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final AccessTokenPolarisHttpClient httpClient = new AccessTokenPolarisHttpClient(logger, 100, true, ProxyInfo.NO_PROXY_INFO, baseUrl, accessToken, gson, new AuthenticationSupport());

        final String requestUri = baseUrl + "/api/common/v0/branches";
        final Request request = new Request.Builder().uri(requestUri).addQueryParameter("page[offset]", "0").addQueryParameter("page[limit]", "10").mimeType("application/vnd.api+json").method(HttpMethod.GET).build();
        try (final Response response = httpClient.execute(request)) {
            logger.info("Response status: " + response.getStatusCode());
            logger.info("Response message: " + response.getStatusMessage());

            final String contentString = response.getContentString();
            final ProjectV0Resources projectV0Resources = gson.fromJson(contentString, ProjectV0Resources.class);
            logger.info("Response content: \n" + projectV0Resources.toString());
        }
    }
}
