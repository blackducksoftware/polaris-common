package com.synopsys.integration.polaris.common.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

public class QueryIssue extends PolarisComponent {
    @SerializedName("attributes")
    private QueryIssueAttributes attributes;

    public QueryIssueAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(final QueryIssueAttributes attributes) {
        this.attributes = attributes;
    }

}
