package com.synopsys.integration.polaris.common;

import java.io.File;
import java.util.Properties;

import com.synopsys.integration.util.IntEnvironmentVariables;

public class SystemPolarisEnvironment implements PolarisEnvironment {
    private IntEnvironmentVariables environmentVariables;
    private Properties systemProperties;
    public SystemPolarisEnvironment(final IntEnvironmentVariables environmentVariables, final Properties systemProperties) {
        this.environmentVariables = environmentVariables;
        this.systemProperties = systemProperties;
    }

    public static SystemPolarisEnvironment withSystemDefaults() {
        return new SystemPolarisEnvironment(new IntEnvironmentVariables(), System.getProperties());
    }

    @Override
    public File getUserHome() {
        return new File(systemProperties.getProperty("user.home"));
    }

    @Override
    public String getEnvironmentVariable(final String name) {
        return environmentVariables.getValue(name);
    }

    @Override
    public boolean containsEnvironmentVariable(final String name) {
        return environmentVariables.containsKey(name);
    }
}
