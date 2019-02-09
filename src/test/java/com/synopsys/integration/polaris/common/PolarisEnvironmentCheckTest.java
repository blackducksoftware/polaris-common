package com.synopsys.integration.polaris.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.util.IntEnvironmentVariables;

public class PolarisEnvironmentCheckTest {
    private Path tempValidTokenDirectoryPath;
    private Path tempInvalidTokenDirectoryPath;

    @BeforeEach
    public void setup() throws IOException {
        tempValidTokenDirectoryPath = Files.createTempDirectory(null);
        tempInvalidTokenDirectoryPath = Files.createTempDirectory(null);
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(tempValidTokenDirectoryPath.toFile());
        FileUtils.deleteDirectory(tempInvalidTokenDirectoryPath.toFile());
    }

    @Test
    public void testValidUserHome() throws IOException {
        PolarisEnvironment polarisEnvironment = Mockito.mock(PolarisEnvironment.class);
        Mockito.when(polarisEnvironment.getUserHome()).thenReturn(createValidAccessTokenDirectory());
        Mockito.when(polarisEnvironment.containsEnvironmentVariable(Mockito.any())).thenReturn(false);

        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(polarisEnvironment);
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertTrue(canRun);
    }

    @Test
    public void testInvalidUserHome() {
        PolarisEnvironment polarisEnvironment = Mockito.mock(PolarisEnvironment.class);
        Mockito.when(polarisEnvironment.getUserHome()).thenReturn(createEmptyAccessTokenDirectory());
        Mockito.when(polarisEnvironment.containsEnvironmentVariable(Mockito.any())).thenReturn(false);

        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(polarisEnvironment);
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertFalse(canRun);
    }

    @Test
    public void testValidEnvVarHome() throws IOException {
        File newHome = createValidAccessTokenDirectory();

        PolarisEnvironment polarisEnvironment = Mockito.mock(PolarisEnvironment.class);
        Mockito.when(polarisEnvironment.getUserHome()).thenReturn(createEmptyAccessTokenDirectory());
        Mockito.when(polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_HOME_ENVIRONMENT_VARIABLE)).thenReturn(true);
        Mockito.when(polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_HOME_ENVIRONMENT_VARIABLE)).thenReturn(newHome.getAbsolutePath());

        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(polarisEnvironment);
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertTrue(canRun);
    }

    @Test
    public void testValidEnvVarFile() throws IOException {
        String tokenName = "different-name";
        File homeDir = createValidAccessTokenDirectory(tokenName);

        PolarisEnvironment polarisEnvironment = Mockito.mock(PolarisEnvironment.class);
        Mockito.when(polarisEnvironment.getUserHome()).thenReturn(homeDir);
        Mockito.when(polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE)).thenReturn(true);
        Mockito.when(polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE)).thenReturn(tokenName);

        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(polarisEnvironment);
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertTrue(canRun);
    }

    @Test
    public void testValidEnvVarToken() throws IOException {
        PolarisEnvironment polarisEnvironment = Mockito.mock(PolarisEnvironment.class);
        Mockito.when(polarisEnvironment.getUserHome()).thenReturn(createEmptyAccessTokenDirectory());
        Mockito.when(polarisEnvironment.containsEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE)).thenReturn(true);
        Mockito.when(polarisEnvironment.getEnvironmentVariable(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE)).thenReturn("any-content");

        PolarisEnvironmentCheck polarisEnvironmentCheck = new PolarisEnvironmentCheck(polarisEnvironment);
        boolean canRun = polarisEnvironmentCheck.isAccessTokenConfigured();
        assertTrue(canRun);
    }


    private File createEmptyAccessTokenDirectory() {
        return tempInvalidTokenDirectoryPath.toFile();
    }

    private File createValidAccessTokenDirectory() throws IOException {
        return createValidAccessTokenDirectory(PolarisEnvironmentCheck.POLARIS_ACCESS_TOKEN_FILENAME_DEFAULT);
    }

    private File createValidAccessTokenDirectory(String filename) throws IOException {
        File tempDirectory = tempValidTokenDirectoryPath.toFile();
        File polarisConfig = new File(tempDirectory, PolarisEnvironmentCheck.POLARIS_CONFIG_DIRECTORY_DEFAULT);
        polarisConfig.mkdirs();
        File polarisAccessToken = new File(polarisConfig, filename);
        try (FileWriter fileWriter = new FileWriter(polarisAccessToken)) {
            fileWriter.append("test content");
        }
        return tempDirectory;
    }

}
