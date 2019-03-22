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
package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisRelationship;
import com.synopsys.integration.polaris.common.api.auth.model.user.EmailDetailsResource;
import com.synopsys.integration.polaris.common.api.auth.model.user.EmailDetailsResources;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResource;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResources;

public class UserService {
    private static final TypeToken USER_RESOURCES = new TypeToken<UserResources>() {};
    private static final TypeToken EMAIL_DETAILS_RESOURCE = new TypeToken<EmailDetailsResources>() {};

    private final AuthService authService;

    public UserService(final AuthService authService) {
        this.authService = authService;
    }

    public List<UserResource> getAllUsers() throws IntegrationException {
        return authService.getAll(AuthService.USERS_API_SPEC, USER_RESOURCES.getType());
    }

    public Optional<String> getEmailForUser(final UserResource user) throws IntegrationException {
        final String email = user.getAttributes().getEmail();
        if (StringUtils.isNotBlank(email)) {
            return Optional.of(email);
        } else {
            final PolarisRelationship emailDetails = user.getRelationships().getEmailDetails();
            return authService.getAttributeFromRelationship(emailDetails.getLinks(), (EmailDetailsResource resource) -> resource.getAttributes().getEmail(), EMAIL_DETAILS_RESOURCE.getType());
        }
    }

}
