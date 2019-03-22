package com.synopsys.integration.polaris.common.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.PolarisResources;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResource;
import com.synopsys.integration.polaris.common.request.param.ParamOperator;
import com.synopsys.integration.polaris.common.request.param.ParamType;
import com.synopsys.integration.polaris.common.request.param.PolarisParamBuilder;

public class RoleAssignmentsService {
    private static final TypeToken ROLE_ASSIGNMENT_RESOURCES = new TypeToken<PolarisResources<?>>() {};

    private final AuthService authService;

    public RoleAssignmentsService(final AuthService authService) {
        this.authService = authService;
    }

    // TODO fix types in polaris-common-api
    public List<?> getAllRoleAssignments() throws IntegrationException {
        return authService.getAll(AuthService.ROLE_ASSIGNMENTS_API_SPEC, ROLE_ASSIGNMENT_RESOURCES.getType());
    }

    // TODO fix types in polaris-common-api
    public List<?> getFilteredRoleAssignments(final PolarisParamBuilder polarisParamBuilder) throws IntegrationException {
        return getFilteredRoleAssignments(Arrays.asList(polarisParamBuilder));
    }

    // TODO fix types in polaris-common-api
    public List<?> getFilteredRoleAssignments(final Collection<PolarisParamBuilder> polarisParamBuilders) throws IntegrationException {
        return authService.getFiltered(AuthService.ROLE_ASSIGNMENTS_API_SPEC, polarisParamBuilders, ROLE_ASSIGNMENT_RESOURCES.getType());
    }

    public List<UserResource> getUsersForProject(final String projectId) throws IntegrationException {
        final PolarisParamBuilder projectFilter = createProjectFilter(projectId);
        final PolarisParamBuilder includeUser = createIncludeFilter("user");
        final PolarisParamBuilder includeRole = createIncludeFilter("role");

        final List<PolarisParamBuilder> paramBuilders = Arrays.asList(projectFilter, includeUser, includeRole);
        final Object object = getFilteredRoleAssignments(paramBuilders);
        // TODO implement
        return null;
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
                   .setOperator(ParamOperator.NONE)
                   .addAdditionalProp("role-assignments")
                   .setCaseSensitive(true);
    }

}
