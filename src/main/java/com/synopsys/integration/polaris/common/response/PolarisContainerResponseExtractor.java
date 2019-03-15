package com.synopsys.integration.polaris.common.response;

import java.util.List;
import java.util.function.Function;

import com.synopsys.integration.polaris.common.api.generated.common.ResourcesPagination;

public class PolarisContainerResponseExtractor<W, R> {
    private Function<W, List<R>> getResponseList;
    private Function<W, ResourcesPagination> getMetaFunction;

    public PolarisContainerResponseExtractor(final Function<W, List<R>> getResponseList, final Function<W, ResourcesPagination> getMetaFunction) {
        this.getResponseList = getResponseList;
        this.getMetaFunction = getMetaFunction;
    }

    public Function<W, List<R>> getGetResponseList() {
        return getResponseList;
    }

    public Function<W, ResourcesPagination> getGetMetaFunction() {
        return getMetaFunction;
    }

}
