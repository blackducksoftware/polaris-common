/**
 * polaris-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.polaris.common.cli.model;

import com.google.gson.annotations.SerializedName;

public class PolarisCliResponseModel {
    private String version;
    private ScanInfo scanInfo;
    private ProjectInfo projectInfo;
    private IssueSummary issueSummary;

    @SerializedName("coverity")
    private CoverityToolInfo coverityToolInfo;

    @SerializedName("sca")
    private BlackDuckScaToolInfo blackDuckScaToolInfo;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public ScanInfo getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(final ScanInfo scanInfo) {
        this.scanInfo = scanInfo;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(final ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public IssueSummary getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(final IssueSummary issueSummary) {
        this.issueSummary = issueSummary;
    }

    public CoverityToolInfo getCoverityToolInfo() {
        return coverityToolInfo;
    }

    public void setCoverityToolInfo(final CoverityToolInfo coverityToolInfo) {
        this.coverityToolInfo = coverityToolInfo;
    }

    public BlackDuckScaToolInfo getBlackDuckScaToolInfo() {
        return blackDuckScaToolInfo;
    }

    public void setBlackDuckScaToolInfo(final BlackDuckScaToolInfo blackDuckScaToolInfo) {
        this.blackDuckScaToolInfo = blackDuckScaToolInfo;
    }

}
