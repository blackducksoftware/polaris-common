package com.synopsys.integration.polaris.common.request;

import java.util.function.BiFunction;

import com.synopsys.integration.polaris.common.response.PolarisContainerResponseExtractor;
import com.synopsys.integration.rest.request.Request;

public class PolarisPagedRequestCreator<W, R> {
    private BiFunction<Integer, Integer, Request> createPagedRequest;
    private PolarisContainerResponseExtractor<W, R> polarisContainerResponseExtractor;

    public PolarisPagedRequestCreator(final BiFunction<Integer, Integer, Request> createPagedRequest, final PolarisContainerResponseExtractor<W, R> polarisContainerResponseExtractor) {
        this.createPagedRequest = createPagedRequest;
        this.polarisContainerResponseExtractor = polarisContainerResponseExtractor;
    }

    public BiFunction<Integer, Integer, Request> getCreatePagedRequest() {
        return createPagedRequest;
    }

    public PolarisContainerResponseExtractor<W, R> getPolarisContainerResponseExtractor() {
        return polarisContainerResponseExtractor;
    }

}
