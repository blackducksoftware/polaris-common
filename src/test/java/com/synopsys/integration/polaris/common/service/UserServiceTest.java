package com.synopsys.integration.polaris.common.service;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.auth.model.user.UserResource;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;

public class UserServiceTest {
    @Test
    public void callGetAllUsersAndGetEmailTest() throws IntegrationException {
        final PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setUrl(System.getenv("POLARIS_URL"));
        polarisServerConfigBuilder.setAccessToken(System.getenv("POLARIS_ACCESS_TOKEN"));
        polarisServerConfigBuilder.setGson(new Gson());

        final PolarisServerConfig polarisServerConfig = polarisServerConfigBuilder.build();
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);

        final UserService userService = polarisServicesFactory.createUserService();
        final List<UserResource> users = userService.getAllUsers();

        if (!users.isEmpty()) {
            final UserResource user = users
                                          .stream()
                                          .findAny()
                                          .orElseThrow(() -> new AssertionError("Missing list element"));
            final Optional<String> optionalEmail = userService.getEmailForUser(user);
            optionalEmail.ifPresent(email -> assertTrue("Expected email not to be blank", StringUtils.isNotBlank(email)));
        }
    }

}
