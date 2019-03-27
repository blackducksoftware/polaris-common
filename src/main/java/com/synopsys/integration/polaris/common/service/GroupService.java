package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.polaris.common.api.auth.model.group.GroupResource;
import com.synopsys.integration.polaris.common.api.auth.model.group.GroupResources;
import com.synopsys.integration.polaris.common.request.param.ParamOperator;
import com.synopsys.integration.polaris.common.request.param.ParamType;
import com.synopsys.integration.polaris.common.request.param.PolarisParamBuilder;

public class GroupService {
    private static final TypeToken GROUP_RESOURCES = new TypeToken<GroupResources>() {};

    private final AuthService authService;

    public GroupService(final AuthService authService) {
        this.authService = authService;
    }

    public List<GroupResource> getAllGroups() throws IntegrationException {
        return authService.getAll(AuthService.GROUPS_API_SPEC, GROUP_RESOURCES.getType());
    }

    public Optional<GroupResource> getGroupByName(final String groupName) throws IntegrationException {
        final PolarisParamBuilder groupNameFilter = createGroupNameFilter(groupName);
        final List<GroupResource> filteredGroups = authService.getFiltered(AuthService.GROUPS_API_SPEC, groupNameFilter, GROUP_RESOURCES.getType());
        return filteredGroups
                   .stream()
                   .findFirst();
    }

    private PolarisParamBuilder createGroupNameFilter(final String groupName) {
        return new PolarisParamBuilder()
                   .setValue(groupName)
                   .setParamType(ParamType.FILTER)
                   .setOperator(ParamOperator.OPERATOR_EQUALS)
                   .addAdditionalProp("groups")
                   .addAdditionalProp("groupname")
                   .setCaseSensitive(true);
    }

}
