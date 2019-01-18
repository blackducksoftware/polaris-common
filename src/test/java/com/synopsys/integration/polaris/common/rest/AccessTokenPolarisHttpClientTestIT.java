package com.synopsys.integration.polaris.common.rest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class AccessTokenPolarisHttpClientTestIT {
    public static final String ENV_POLARIS_BASE_URL = "POLARIS_BASE_URL";
    public static final String ENV_POLARIS_ACCESS_TOKEN = "POLARIS_ACCESS_TOKEN";

    private static final String VALID_SPEC = "/api/common/v0/branches?page[offset]=0&page[limit]=10";
    private static final String VALID_MIME_TYPE = "application/vnd.api+json";
    private static final String INVALID_MIME_TYPE = "application/x-www-form-urlencoded";

    private String baseUrl;
    private String accessToken;
    private AuthenticationSupport authenticationSupport;
    private Gson gson;

    @BeforeEach
    public void setup() {
        baseUrl = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_BASE_URL);
        accessToken = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_ACCESS_TOKEN);
        authenticationSupport = new AuthenticationSupport();
        gson = new Gson();
    }

    @Test
    public void validRequestTest() throws IntegrationException, IOException {
        assumeTrue(StringUtils.isNotBlank(baseUrl));
        assumeTrue(StringUtils.isNotBlank(accessToken));

        AccessTokenPolarisHttpClient httpClient = new AccessTokenPolarisHttpClient(new PrintStreamIntLogger(System.out, LogLevel.INFO), 300, true, ProxyInfo.NO_PROXY_INFO, baseUrl, accessToken, gson, authenticationSupport);

        String requestUrl = baseUrl + AccessTokenPolarisHttpClientTestIT.VALID_SPEC;
        Request request = new Request.Builder().method(HttpMethod.GET).uri(requestUrl).mimeType(AccessTokenPolarisHttpClientTestIT.VALID_MIME_TYPE).build();
        try (Response response = httpClient.execute(request)) {
            assertTrue(response.isStatusCodeOkay(), "Status code was not OK");
            System.out.println(response.getContentString());
        }
    }

    @Test
    public void unauthorizedTest() throws IntegrationException, IOException {
        AccessTokenPolarisHttpClient httpClient = new AccessTokenPolarisHttpClient(new PrintStreamIntLogger(System.out, LogLevel.INFO), 300, true, ProxyInfo.NO_PROXY_INFO, baseUrl, accessToken, gson, authenticationSupport);

        String authHeader = "Authorization";
        HttpUriRequest request = Mockito.mock(HttpUriRequest.class);
        Mockito.when(request.containsHeader(authHeader)).thenReturn(true);
        Mockito.doNothing().when(request).removeHeaders(authHeader);

        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatusCode()).thenReturn(RestConstants.UNAUTHORIZED_401);

        httpClient.handleErrorResponse(request, response);
        Mockito.verify(request, Mockito.times(1)).removeHeaders(authHeader);
        Mockito.verify(response, Mockito.times(1)).getStatusCode();
    }

    @Test
    public void invalidMimeTypeTest() throws IntegrationException, IOException {
        assumeTrue(StringUtils.isNotBlank(baseUrl));
        assumeTrue(StringUtils.isNotBlank(accessToken));

        AccessTokenPolarisHttpClient httpClient = new AccessTokenPolarisHttpClient(new PrintStreamIntLogger(System.out, LogLevel.INFO), 300, true, ProxyInfo.NO_PROXY_INFO, baseUrl, accessToken, gson, authenticationSupport);

        String requestUrl = baseUrl + AccessTokenPolarisHttpClientTestIT.VALID_SPEC;
        Request request = new Request.Builder().method(HttpMethod.GET).uri(requestUrl).mimeType(AccessTokenPolarisHttpClientTestIT.INVALID_MIME_TYPE).build();
        try (Response response = httpClient.execute(request)) {
            assertTrue(response.isStatusCodeError(), "Status code was not an error");
        }
    }

}
