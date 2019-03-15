package com.synopsys.integration.polaris.common.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
import com.synopsys.integration.polaris.common.api.generated.common.ResourcesPagination;

public class QueryIssues extends PolarisComponent {
    @SerializedName("data")
    private List<QueryIssue> data = new ArrayList<>();

    @SerializedName("meta")
    private ResourcesPagination meta = null;

    public List<QueryIssue> getData() {
        return data;
    }

    public void setData(final List<QueryIssue> data) {
        this.data = data;
    }

    public ResourcesPagination getMeta() {
        return meta;
    }

    public void setMeta(final ResourcesPagination meta) {
        this.meta = meta;
    }

}
