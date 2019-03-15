package com.synopsys.integration.polaris.common.model;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.api.PolarisComponent;

public class QueryIssueAttributes extends PolarisComponent {
    @SerializedName("finding-key")
    private String findingKey;

    @SerializedName("issue-key")
    private String issueKey;

    @SerializedName("sub-tool")
    private String subTool;

    public String getFindingKey() {
        return findingKey;
    }

    public void setFindingKey(final String findingKey) {
        this.findingKey = findingKey;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(final String issueKey) {
        this.issueKey = issueKey;
    }

    public String getSubTool() {
        return subTool;
    }

    public void setSubTool(final String subTool) {
        this.subTool = subTool;
    }

}
