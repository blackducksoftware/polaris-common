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
package com.synopsys.integration.polaris.common.response;

import java.util.List;
import java.util.function.Function;

import com.synopsys.integration.polaris.common.api.generated.common.ResourcesPagination;

public class PolarisContainerResponseExtractor<W, R> {
    private Function<W, List<R>> getResponseList;
    private Function<W, ResourcesPagination> getMetaFunction;

    public PolarisContainerResponseExtractor(final Function<W, List<R>> getResponseList, final Function<W, ResourcesPagination> getMetaFunction) {
        this.getResponseList = getResponseList;
        this.getMetaFunction = getMetaFunction;
    }

    public Function<W, List<R>> getGetResponseList() {
        return getResponseList;
    }

    public Function<W, ResourcesPagination> getGetMetaFunction() {
        return getMetaFunction;
    }

}
