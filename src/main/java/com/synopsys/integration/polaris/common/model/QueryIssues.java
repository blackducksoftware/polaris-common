/**
 * polaris-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
