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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisResource;
import com.synopsys.integration.polaris.common.api.PolarisResourceSparse;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentRelationship;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentRelationships;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentResource;
import com.synopsys.integration.polaris.common.api.auth.model.role.assignments.RoleAssignmentResources;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResource;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestCreator;
import com.synopsys.integration.polaris.common.request.PolarisPagedRequestWrapper;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.request.param.ParamOperator;
import com.synopsys.integration.polaris.common.request.param.ParamType;
import com.synopsys.integration.polaris.common.request.param.PolarisParamBuilder;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;

public class RoleAssignmentsService {
    public static final String INCLUDE_USERS = "user";
    public static final String INCLUDE_ROLES = "role";

    private static final TypeToken ROLE_ASSIGNMENT_RESOURCES = new TypeToken<RoleAssignmentResources>() {};

    final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;
    private final AuthService authService;

    public RoleAssignmentsService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService, final AuthService authService) {
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
        this.authService = authService;
    }

    public List<RoleAssignmentResource> getAll() throws IntegrationException {
        return authService.getAll(AuthService.ROLE_ASSIGNMENTS_API_SPEC, ROLE_ASSIGNMENT_RESOURCES.getType());
    }

    public List<RoleAssignmentResource> getFiltered(final PolarisParamBuilder polarisParamBuilder) throws IntegrationException {
        return getFiltered(Arrays.asList(polarisParamBuilder));
    }

    public List<RoleAssignmentResource> getFiltered(final Collection<PolarisParamBuilder> polarisParamBuilders) throws IntegrationException {
        return authService.getFiltered(AuthService.ROLE_ASSIGNMENTS_API_SPEC, polarisParamBuilders, ROLE_ASSIGNMENT_RESOURCES.getType());
    }

    public RoleAssignmentResources getRoleAssignmentsForProjectWithIncluded(final String projectId, final String... included) throws IntegrationException {
        final Map<String, Set<String>> queryParameters = new HashMap<>();
        for (final String include : included) {
            final PolarisParamBuilder includeFilter = createIncludeFilter(include);
            final Map.Entry<String, String> queryParam = includeFilter.build();
            queryParameters.computeIfAbsent(queryParam.getKey(), k -> new HashSet<>()).add(queryParam.getValue());
        }

        final Map.Entry<String, String> projectParam = createProjectFilter(projectId).build();
        queryParameters.computeIfAbsent(projectParam.getKey(), k -> new HashSet<>()).add(projectParam.getValue());

        final String uri = polarisHttpClient.getPolarisServerUrl() + AuthService.ROLE_ASSIGNMENTS_API_SPEC;
        final PolarisPagedRequestCreator requestCreator = (limit, offset) -> PolarisRequestFactory.createDefaultPagedRequestBuilder(limit, offset).uri(uri).queryParameters(queryParameters).build();
        final PolarisPagedRequestWrapper pagedRequestWrapper = new PolarisPagedRequestWrapper(requestCreator, ROLE_ASSIGNMENT_RESOURCES.getType());
        return polarisService.getPopulatedResponse(pagedRequestWrapper);
    }

    public Optional<UserResource> getUserFromPopulatedRoleAssignments(final RoleAssignmentResources populatedResources, final RoleAssignmentResource resourceReferenced) {
        return getUserFromPopulatedRoleAssignments(populatedResources, resourceReferenced, RoleAssignmentRelationships::getUser, UserResource.class);
    }

    public PolarisParamBuilder createProjectFilter(final String projectId) {
        return new PolarisParamBuilder()
                   .setValue("project:" + projectId)
                   .setParamType(ParamType.FILTER)
                   .setOperator(ParamOperator.OPERATOR_SUBSTRING)
                   .addAdditionalProp("role-assignments")
                   .addAdditionalProp("object")
                   .setCaseSensitive(false);
    }

    public PolarisParamBuilder createIncludeFilter(final String type) {
        return new PolarisParamBuilder()
                   .setValue(type)
                   .setParamType(ParamType.INCLUDE)
                   .setOperator(ParamOperator.BLANK)
                   .addAdditionalProp("role-assignments")
                   .setCaseSensitive(true);
    }

    private <R extends PolarisResource> Optional<R> getUserFromPopulatedRoleAssignments(final RoleAssignmentResources populatedResources, final RoleAssignmentResource resourceReferenced,
        final Function<RoleAssignmentRelationships, RoleAssignmentRelationship> relationshipRetriever, final Class<R> resourceClass) {
        final Optional<PolarisResourceSparse> optionalUserData = relationshipRetriever.apply(resourceReferenced.getRelationships()).getData();
        if (optionalUserData.isPresent()) {
            final String id = optionalUserData.map(PolarisResourceSparse::getId).orElse("");
            final String type = optionalUserData.map(PolarisResourceSparse::getType).orElse("");
            for (final PolarisResource includedResource : populatedResources.getIncluded()) {
                if (type.equals(includedResource.getType()) && id.equals(includedResource.getId())) {
                    return Optional.of(resourceClass.cast(includedResource));
                }
            }
        }
        return Optional.empty();
    }

}
