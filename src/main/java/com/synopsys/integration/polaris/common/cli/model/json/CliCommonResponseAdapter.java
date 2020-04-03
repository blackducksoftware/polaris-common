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
package com.synopsys.integration.polaris.common.cli.model.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.polaris.common.cli.model.CliCommonResponseModel;
import com.synopsys.integration.polaris.common.cli.model.CommonToolInfo;
import com.synopsys.integration.polaris.common.cli.model.json.v1.CliScanV1;
import com.synopsys.integration.polaris.common.cli.model.json.v1.ToolInfoV1;
import com.synopsys.integration.polaris.common.cli.model.json.v2.CliScanV2;

public class CliCommonResponseAdapter {
    private final Gson gson;

    public CliCommonResponseAdapter(final Gson gson) {
        this.gson = gson;
    }

    public CliCommonResponseModel fromCliScanV2(final CliScanV2 cliScanV2) {
        final CliCommonResponseModel cliCommonResponseModel = new CliCommonResponseModel();
        cliCommonResponseModel.setIssueSummary(cliScanV2.getIssueSummary());
        cliCommonResponseModel.setProjectInfo(cliScanV2.getProjectInfo());
        cliCommonResponseModel.setScanInfo(cliScanV2.getScanInfo());
        final List<CommonToolInfo> tools = Optional.ofNullable(cliScanV2.getTools())
                                               .map(Arrays::asList)
                                               .orElse(Collections.emptyList());
        cliCommonResponseModel.setTools(tools);

        return cliCommonResponseModel;
    }

    public CliCommonResponseModel fromCliScanV1(final CliScanV1 cliScanV1) {
        final CliCommonResponseModel cliCommonResponseModel = new CliCommonResponseModel();
        cliCommonResponseModel.setIssueSummary(cliScanV1.getIssueSummary());
        cliCommonResponseModel.setProjectInfo(cliScanV1.getProjectInfo());
        cliCommonResponseModel.setScanInfo(cliScanV1.getScanInfo());

        final List<CommonToolInfo> tools = new ArrayList<>();
        fromToolInfoV1(cliScanV1.getBlackDuckScaToolInfo(), "sca")
            .ifPresent(tools::add);
        fromToolInfoV1(cliScanV1.getCoverityToolInfo(), "coverity")
            .ifPresent(tools::add);

        cliCommonResponseModel.setTools(tools);

        return cliCommonResponseModel;
    }

    private Optional<CommonToolInfo> fromToolInfoV1(final ToolInfoV1 toolInfoV1, final String toolName) {
        if (toolInfoV1 == null) {
            return Optional.empty();
        }

        //TODO investigate BeanUtils.copyProperties?
        final CommonToolInfo commonToolInfo = new CommonToolInfo();
        commonToolInfo.setIssueApiUrl(toolInfoV1.getIssueApiUrl());
        commonToolInfo.setJobId(toolInfoV1.getJobId());
        commonToolInfo.setJobStatus(toolInfoV1.getJobStatus());
        commonToolInfo.setJobStatusUrl(toolInfoV1.getJobStatusUrl());
        commonToolInfo.setToolVersion(toolInfoV1.getToolVersion());

        commonToolInfo.setToolName(toolName);
        return Optional.of(commonToolInfo);
    }
}
