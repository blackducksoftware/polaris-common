package com.synopsys.integration.polaris.common.cli.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import com.synopsys.integration.polaris.common.cli.model.v1.CliScanV1;
import com.synopsys.integration.polaris.common.cli.model.v1.ToolInfoV1;
import com.synopsys.integration.polaris.common.cli.model.v2.CliScanV2;

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
