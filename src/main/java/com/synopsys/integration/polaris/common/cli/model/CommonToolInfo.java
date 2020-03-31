package com.synopsys.integration.polaris.common.cli.model;

public class CommonToolInfo {
    private String toolName;
    private String toolVersion;
    private String jobId;
    private String jobStatusUrl;
    private String jobStatus;
    private String issueApiUrl;

    public String getToolName() {
        return toolName;
    }

    public void setToolName(final String toolName) {
        this.toolName = toolName;
    }

    public String getToolVersion() {
        return toolVersion;
    }

    public void setToolVersion(final String toolVersion) {
        this.toolVersion = toolVersion;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(final String jobId) {
        this.jobId = jobId;
    }

    public String getJobStatusUrl() {
        return jobStatusUrl;
    }

    public void setJobStatusUrl(final String jobStatusUrl) {
        this.jobStatusUrl = jobStatusUrl;
    }

    public String getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(final String jobStatus) {
        this.jobStatus = jobStatus;
    }

    public String getIssueApiUrl() {
        return issueApiUrl;
    }

    public void setIssueApiUrl(final String issueApiUrl) {
        this.issueApiUrl = issueApiUrl;
    }
}
