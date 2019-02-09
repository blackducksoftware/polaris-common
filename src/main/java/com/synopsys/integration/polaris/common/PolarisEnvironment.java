package com.synopsys.integration.polaris.common;

import java.io.File;

import com.sun.istack.internal.Nullable;

public interface PolarisEnvironment {
    File getUserHome();

    String getEnvironmentVariable(String name);

    boolean containsEnvironmentVariable(String name);

}
