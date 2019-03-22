package com.synopsys.integration.polaris.common.request.param;

public interface ParamEnum {
    String getKey();

    default boolean equalsKey(final String candidate) {
        return getKey().equals(candidate);
    }

}
