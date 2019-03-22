package com.synopsys.integration.polaris.common.request.param;

public enum ParamType implements ParamEnum {
    FILTER("filter"),
    INCLUDE("include"),
    PAGE("page");

    private final String key;

    ParamType(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

}
