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
package com.synopsys.integration.polaris.common.cli.model.json.v1;

import com.google.gson.annotations.SerializedName;
import com.synopsys.integration.polaris.common.cli.model.json.adapter.CliScanResponse;

public class CliScanV1 implements CliScanResponse {
    public String version;
    public ScanInfoV1 scanInfo;
    public ProjectInfoV1 projectInfo;
    public IssueSummaryV1 issueSummary;

    @SerializedName("coverity")
    public ToolInfoV1 coverityToolInfo;

    @SerializedName("sca")
    public ToolInfoV1 blackDuckScaToolInfo;

}
