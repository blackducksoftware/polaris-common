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
package com.synopsys.integration.polaris.common.cli.model.v1;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.cli.model.CommonIssueSummary;
import com.synopsys.integration.polaris.common.cli.model.CommonProjectInfo;
import com.synopsys.integration.polaris.common.cli.model.CommonScanInfo;

public class CliScanV1 {
    private String version;
    private CommonScanInfo scanInfo;
    private CommonProjectInfo projectInfo;
    private CommonIssueSummary issueSummary;

    @SerializedName("coverity")
    private ToolInfoV1 coverityToolInfo;

    @SerializedName("sca")
    private ToolInfoV1 blackDuckScaToolInfo;

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public CommonScanInfo getScanInfo() {
        return scanInfo;
    }

    public void setScanInfo(final CommonScanInfo scanInfo) {
        this.scanInfo = scanInfo;
    }

    public CommonProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(final CommonProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public CommonIssueSummary getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(final CommonIssueSummary issueSummary) {
        this.issueSummary = issueSummary;
    }

    public ToolInfoV1 getCoverityToolInfo() {
        return coverityToolInfo;
    }

    public void setCoverityToolInfo(final ToolInfoV1 coverityToolInfo) {
        this.coverityToolInfo = coverityToolInfo;
    }

    public ToolInfoV1 getBlackDuckScaToolInfo() {
        return blackDuckScaToolInfo;
    }

    public void setBlackDuckScaToolInfo(final ToolInfoV1 blackDuckScaToolInfo) {
        this.blackDuckScaToolInfo = blackDuckScaToolInfo;
    }

}
