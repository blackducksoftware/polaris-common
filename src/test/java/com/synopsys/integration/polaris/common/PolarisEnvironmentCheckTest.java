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

import com.synopsys.integration.util.IntEnvironmentVariables;

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
        IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(intEnvironmentVariables, tempDirectoryPath.toFile());
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertFalse(canRun);
    }

    @Test
    public void testValidEnvironment() throws IOException {
        File tempDirectory = tempDirectoryPath.toFile();
        File polarisConfig = new File(tempDirectory, PolarisEnvironmentCheck.POLARIS_CONFIG_DIRECTORY_DEFAULT);
        polarisConfig.mkdirs();
        File polarisAccessToken = new File(polarisConfig, PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILENAME_DEFAULT);
        try (FileWriter fileWriter = new FileWriter(polarisAccessToken)) {
            fileWriter.append("test content");
        }

        IntEnvironmentVariables intEnvironmentVariables = new IntEnvironmentVariables();
        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(intEnvironmentVariables, tempDirectory);

        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertTrue(canRun);
    }

}
