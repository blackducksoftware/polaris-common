package com.synopsys.integration.polaris.common.configuration;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

public class PolarisServerConfigBuilderTest {
    @Test
    public void testSimpleValidConfig() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = createBuilderWithoutAccessToken();

        polarisServerConfigBuilder.setAccessToken("fake but valid (not blank) access token");

        PolarisServerConfig polarisServerConfig = polarisServerConfigBuilder.build();
        assertNotNull(polarisServerConfig);
    }

    @Test
    public void testPopulatedFromEnvironment() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();

        Map<String, String> fakeEnvironment = new HashMap<>();
        fakeEnvironment.put("polaris.access.token", "fake but valid (not blank) access token");
        fakeEnvironment.put("polaris.url", "http://www.google.com/fake_but_valid_not_blank_url");
        fakeEnvironment.put("polaris.timeout.in.seconds", "120");
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);

        assertTrue(polarisServerConfigBuilder.isValid());
        assertEquals("fake but valid (not blank) access token", polarisServerConfigBuilder.getAccessToken());
        assertEquals("http://www.google.com/fake_but_valid_not_blank_url", polarisServerConfigBuilder.getPolarisUrl());
        assertEquals(120, polarisServerConfigBuilder.getTimeoutSeconds());
    }

    @Test
    public void testInvalidTimeoutFromEnvironment() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();

        Map<String, String> fakeEnvironment = new HashMap<>();
        fakeEnvironment.put("polaris.timeout.in.seconds", "invalid - not numeric");
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);

        assertEquals(0, polarisServerConfigBuilder.getTimeoutSeconds());
    }

    @Test
    public void testUrlConfigFromEnvironment() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setAccessToken("fake but valid (not blank) access token");
        polarisServerConfigBuilder.setTimeoutSeconds(120);

        assertFalse(polarisServerConfigBuilder.isValid());

        Map<String, String> fakeEnvironment = new HashMap<>();
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);
        assertFalse(polarisServerConfigBuilder.isValid());

        fakeEnvironment.put("POLARIS_URL", "http://www.google.com/fake_but_valid_not_blank_url");
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);
        assertTrue(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setAccessToken("fake but valid (not blank) access token");
        polarisServerConfigBuilder.setTimeoutSeconds(120);

        fakeEnvironment = new HashMap<>();
        fakeEnvironment.put("polaris.url", "http://www.google.com/fake_but_valid_not_blank_url");
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);
        assertTrue(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setAccessToken("fake but valid (not blank) access token");
        polarisServerConfigBuilder.setTimeoutSeconds(120);

        fakeEnvironment = new HashMap<>();
        fakeEnvironment.put("swip.server.url", "http://www.google.com/fake_but_valid_not_blank_url");
        polarisServerConfigBuilder.setFromProperties(fakeEnvironment);
        assertTrue(polarisServerConfigBuilder.isValid());
    }

    @Test
    public void testAllKeysGenerated() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        List<String> allKeys = polarisServerConfigBuilder.getAllPropertyKeys();
        assertEquals(36, allKeys.size());
    }

    @Test
    public void testResolveAccessTokenFromUserHome() throws IOException {
        Path testUserHome = Files.createTempDirectory("polarisBuilder_userhome");
        File polarisHome = new File(testUserHome.toFile(), ".swip");
        polarisHome.mkdirs();
        File accessTokenFile = new File(polarisHome, ".access_token");
        accessTokenFile.createNewFile();
        FileUtils.writeStringToFile(accessTokenFile, "fake but valid not blank access token", StandardCharsets.UTF_8);
        Map<String, String> properties = new HashMap<>();
        properties.put("user.home", testUserHome.toString());

        PolarisServerConfigBuilder polarisServerConfigBuilder = createBuilderWithoutAccessToken();
        assertFalse(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder.setFromProperties(properties);
        assertTrue(polarisServerConfigBuilder.isValid());
    }

    @Test
    public void testResolveAccessTokenFromPolarisHomeEnvVar() throws IOException {
        Path testPolarisHome = Files.createTempDirectory("polarisBuilder_polarishome");
        File accessTokenFile = new File(testPolarisHome.toFile(), ".access_token");
        accessTokenFile.createNewFile();
        FileUtils.writeStringToFile(accessTokenFile, "fake but valid not blank access token", StandardCharsets.UTF_8);
        Map<String, String> properties = new HashMap<>();
        properties.put("polaris.home", testPolarisHome.toString());

        PolarisServerConfigBuilder polarisServerConfigBuilder = createBuilderWithoutAccessToken();
        assertFalse(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder.setFromProperties(properties);
        assertTrue(polarisServerConfigBuilder.isValid());
    }

    @Test
    public void testResolveAccessTokenFromFilePath() throws IOException {
        Path testAccessToken = Files.createTempFile("polarisBuilder_access_token", null);
        FileUtils.writeStringToFile(testAccessToken.toFile(), "fake but valid not blank access token", StandardCharsets.UTF_8);
        Map<String, String> properties = new HashMap<>();
        properties.put("polaris.access.token.file", testAccessToken.toString());

        PolarisServerConfigBuilder polarisServerConfigBuilder = createBuilderWithoutAccessToken();
        assertFalse(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder.setFromProperties(properties);
        assertTrue(polarisServerConfigBuilder.isValid());

        PolarisServerConfig polarisServerConfig = polarisServerConfigBuilder.build();
        assertEquals("fake but valid not blank access token", polarisServerConfig.getAccessToken());
    }

    @Test
    public void testResolveAccessTokenFromEnvVar() {
        Map<String, String> properties = new HashMap<>();
        properties.put("polaris.access.token", "fake but valid not blank access token");

        PolarisServerConfigBuilder polarisServerConfigBuilder = createBuilderWithoutAccessToken();
        assertFalse(polarisServerConfigBuilder.isValid());

        polarisServerConfigBuilder.setFromProperties(properties);
        assertTrue(polarisServerConfigBuilder.isValid());
    }

    private PolarisServerConfigBuilder createBuilderWithoutAccessToken() {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();

        polarisServerConfigBuilder.setPolarisUrl("http://www.google.com/fake_but_valid_not_blank_url");
        polarisServerConfigBuilder.setTimeoutSeconds(120);

        return polarisServerConfigBuilder;
    }

}
