package com.synopsys.integration.polaris.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PolarisEnvironmentCheckTest {
    private Path tempDirectoryPath;

    @BeforeEach
    public void setup() throws IOException {
        tempDirectoryPath = Files.createTempDirectory(null);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempDirectoryPath.toFile());
    }

    @Test
    public void testInvalidEnvironment() {
        final PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck();
        final boolean canRun = polarisEnvironmentCheck.canRun(tempDirectoryPath.toFile());
        assertFalse(canRun);
    }

    @Test
    public void testValidEnvironment() throws IOException {
        final File tempDirectory = tempDirectoryPath.toFile();
        final File polarisConfig = new File(tempDirectory, PolarisEnvironmentCheck.POLARIS_CONFIG_DIRECTORY);
        polarisConfig.mkdirs();
        final File polarisAccessToken = new File(polarisConfig, PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILENAME);
        try (FileWriter fileWriter = new FileWriter(polarisAccessToken)) {
            fileWriter.append("test content");
        }

        final PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck();
        final boolean canRun = polarisEnvironmentCheck.canRun(tempDirectoryPath.toFile());
        assertTrue(canRun);
    }

}
