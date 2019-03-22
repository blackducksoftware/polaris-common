package com.synopsys.integration.polaris.common.request.param;

public enum ParamOperator implements ParamEnum {
    OPERATOR_EQUALS("eq"),
    OPERATOR_SUBSTRING("substr"),
    OPERATOR_ONE_OF("one-of"),
    OPERATOR_GREATER_THAN_OR_EQUAL("gte"),
    OPERATOR_LESS_THAN_OR_EQUAL("lte"),
    BLANK(""),
    NONE("");

    private final String key;

    ParamOperator(final String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

}
