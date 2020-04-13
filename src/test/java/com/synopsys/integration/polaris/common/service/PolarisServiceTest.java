package com.synopsys.integration.polaris.common.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.PolarisResources;
import com.synopsys.integration.polaris.common.api.PolarisResourcesPagination;
import com.synopsys.integration.polaris.common.api.common.model.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.model.branch.BranchV0Resources;
import com.synopsys.integration.polaris.common.api.common.model.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.api.common.model.project.ProjectV0Resources;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClientTestIT;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;

public class PolarisServiceTest {
    private final PolarisRequestFactory polarisRequestFactory = new PolarisRequestFactory();

    @Test
    public void createDefaultPolarisGetRequestTest() {
        final Request request = polarisRequestFactory.createDefaultPolarisGetRequest("https://google.com");
        assertNotNull(request);
    }

    @Test
    public void executeGetRequestTestIT() throws IntegrationException {
        final String baseUrl = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_URL);
        final String accessToken = System.getenv(AccessTokenPolarisHttpClientTestIT.ENV_POLARIS_ACCESS_TOKEN);

        assumeTrue(StringUtils.isNotBlank(baseUrl));
        assumeTrue(StringUtils.isNotBlank(accessToken));

        final Gson gson = new Gson();
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final AccessTokenPolarisHttpClient httpClient = new AccessTokenPolarisHttpClient(logger, 100, true, ProxyInfo.NO_PROXY_INFO, baseUrl, accessToken, gson, new AuthenticationSupport());

        final PolarisService polarisService = new PolarisService(httpClient, new PolarisJsonTransformer(gson, logger), PolarisRequestFactory.DEFAULT_LIMIT);

        final String requestUri = baseUrl + "/api/common/v0/branches";
        final Request request = polarisRequestFactory.createDefaultPolarisGetRequest(requestUri);

        final PolarisResources<BranchV0Resource> branchV0Resources = polarisService.get(BranchV0Resources.class, request);
        final List<BranchV0Resource> branchV0ResourceList = branchV0Resources.getData();
        assertNotNull(branchV0ResourceList);
        final PolarisResourcesPagination meta = branchV0Resources.getMeta();
        assertNotNull(meta);
    }

    @Test
    public void testGettingAllOnePageTotal() throws IOException, IntegrationException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projects_all_on_one_page.json");

        AccessTokenPolarisHttpClient polarisHttpClient = Mockito.mock(AccessTokenPolarisHttpClient.class);
        mockClientBehavior(polarisHttpClient, offsetsToResults, 100);

        PolarisJsonTransformer polarisJsonTransformer = new PolarisJsonTransformer(PolarisServicesFactory.createDefaultGson(), new PrintStreamIntLogger(System.out, LogLevel.INFO));

        final String requestUri = PolarisService.PROJECT_API_SPEC;
        final PolarisPagedRequestCreator requestCreator = (limit, offset) -> PolarisRequestFactory.createDefaultPagedRequestBuilder(limit, offset)
                                                                                 .uri(requestUri)
                                                                                 .build();
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(requestCreator, ProjectV0Resources.class);

        PolarisService polarisService = new PolarisService(polarisHttpClient, polarisJsonTransformer, PolarisRequestFactory.DEFAULT_LIMIT);

        List<ProjectV0Resource> allPagesResponse = polarisService.getAllResponses(pagedRequestWrapper);
        assertEquals(16, allPagesResponse.size());
    }

    @Test
    public void testGettingAllMultiplePagesTotal() throws IntegrationException, IOException {
        Map<String, String> offsetsToResults = new HashMap<>();
        offsetsToResults.put("0", "projects_page_1_of_10.json");
        offsetsToResults.put("25", "projects_page_2_of_10.json");
        offsetsToResults.put("50", "projects_page_3_of_10.json");
        offsetsToResults.put("75", "projects_page_4_of_10.json");
        offsetsToResults.put("100", "projects_page_5_of_10.json");
        offsetsToResults.put("125", "projects_page_6_of_10.json");
        offsetsToResults.put("150", "projects_page_7_of_10.json");
        offsetsToResults.put("175", "projects_page_8_of_10.json");
        offsetsToResults.put("200", "projects_page_9_of_10.json");
        offsetsToResults.put("225", "projects_page_10_of_10.json");

        AccessTokenPolarisHttpClient polarisHttpClient = Mockito.mock(AccessTokenPolarisHttpClient.class);
        mockClientBehavior(polarisHttpClient, offsetsToResults, 100);

        PolarisJsonTransformer polarisJsonTransformer = new PolarisJsonTransformer(PolarisServicesFactory.createDefaultGson(), new PrintStreamIntLogger(System.out, LogLevel.INFO));

        final String requestUri = PolarisService.PROJECT_API_SPEC;
        final PolarisPagedRequestCreator requestCreator = (limit, offset) -> PolarisRequestFactory.createDefaultPagedRequestBuilder(limit, offset)
                                                                                 .uri(requestUri)
                                                                                 .build();
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(requestCreator, ProjectV0Resources.class);

        PolarisService polarisService = new PolarisService(polarisHttpClient, polarisJsonTransformer, PolarisRequestFactory.DEFAULT_LIMIT);

        List<ProjectV0Resource> allPagesResponse = polarisService.getAllResponses(pagedRequestWrapper);
        assertEquals(241, allPagesResponse.size());
    }

    private void mockClientBehavior(AccessTokenPolarisHttpClient polarisHttpClient, Map<String, String> offsetsToResults, int limit) throws IOException, IntegrationException {
        for (Map.Entry<String, String> entry : offsetsToResults.entrySet()) {
            Response response = Mockito.mock(Response.class);
            Mockito.when(response.getContentString()).thenReturn(getText(entry.getValue()));

            ArgumentMatcher<Request> argRequest = createRequestMatcher(PolarisService.PROJECT_API_SPEC, Integer.parseInt(entry.getKey()), limit);
            Mockito.when(polarisHttpClient.execute(Mockito.argThat(argRequest))).thenReturn(response);
        }
    }

    private ArgumentMatcher<Request> createRequestMatcher(String uri, int offset, int limit) {
        return new ArgumentMatcher<Request>() {
            @Override
            public boolean matches(Request request) {
                if (null != request && request.getUri().equals(uri)) {
                    String requestOffset = request.getQueryParameters().get(PolarisRequestFactory.OFFSET_PARAMETER).stream().findFirst().get();
                    return requestOffset.equals(Integer.toString(offset));
                }
                return false;
            }
        };
    }

    private String getText(String resourceName) throws IOException {
        return IOUtils.toString(getClass().getResourceAsStream("/PolarisService/" + resourceName), StandardCharsets.UTF_8);
    }

}
