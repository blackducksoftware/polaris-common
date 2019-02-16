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
package com.synopsys.integration.polaris.common.configuration;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.gson.Gson;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.BuilderStatus;
import com.synopsys.integration.util.IntegrationBuilder;

public class PolarisServerConfigBuilder extends IntegrationBuilder<PolarisServerConfig> {
    public static final String POLARIS_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX = "POLARIS_";
    public static final String POLARIS_SERVER_CONFIG_PROPERTY_KEY_PREFIX = "polaris.";

    //Polaris home directory to use. Defaults to $HOME/.swip.
    public static final String POLARIS_HOME_ENVIRONMENT_VARIABLE = "SWIP_HOME";

    //Access token file to use. Defaults to $SWIP_HOME/.access_token.
    public static final String POLARIS_ACCESS_TOKEN_FILE_ENVIRONMENT_VARIABLE = "SWIP_ACCESS_TOKEN_FILE";

    public static final String POLARIS_ACCESS_TOKEN_ENVIRONMENT_VARIABLE = "SWIP_ACCESS_TOKEN";
    public static final String POLARIS_SERVER_URL_ENVIRONMENT_VARIABLE = "SWIP_SERVER_URL";

    public static final String POLARIS_CONFIG_DIRECTORY_DEFAULT = ".swip";
    public static final String POLARIS_ACCESS_TOKEN_FILENAME_DEFAULT = ".access_token";

    private String polarisUrl;
    private int timeoutSeconds;
    private String accessToken;
    private boolean trustCert;

    private String proxyHost;
    private int proxyPort;
    private String proxyUsername;
    private String proxyPassword;
    private String proxyNtlmDomain;
    private String proxyNtlmWorkstation;

    // these properties are alternative ways to find the access token
    private String polarisHome;
    private String accessTokenFilePath;
    private String userHomePath;

    private IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
    private Gson gson = new Gson();
    private AuthenticationSupport authenticationSupport = new AuthenticationSupport();

    @Override
    protected PolarisServerConfig buildWithoutValidation() {
        URL polarisURL = null;
        try {
            polarisURL = new URL(polarisUrl);
        } catch (MalformedURLException e) {
        }

        return new PolarisServerConfig(polarisURL, timeoutSeconds, accessToken, getProxyInfo(), trustCert, gson, authenticationSupport);
    }

    private ProxyInfo getProxyInfo() {
        if (StringUtils.isBlank(proxyHost)) {
            return ProxyInfo.NO_PROXY_INFO;
        }

        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(proxyUsername, proxyPassword);
        Credentials proxyCredentials = credentialsBuilder.build();

        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost(proxyHost);
        proxyInfoBuilder.setPort(proxyPort);
        proxyInfoBuilder.setCredentials(proxyCredentials);
        proxyInfoBuilder.setNtlmDomain(proxyNtlmDomain);
        proxyInfoBuilder.setNtlmWorkstation(proxyNtlmWorkstation);

        return proxyInfoBuilder.build();
    }

    @Override
    protected void validate(BuilderStatus builderStatus) {
        if (StringUtils.isBlank(polarisUrl)) {
            builderStatus.addErrorMessage("The Polaris url must be specified.");
        } else {
            try {
                URL blackDuckURL = new URL(polarisUrl);
                blackDuckURL.toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                builderStatus.addErrorMessage(String.format("The provided Polaris url (%s) is not a valid URL.", polarisUrl));
            }
        }

        PolarisAccessTokenResolver accessTokenResolver = new PolarisAccessTokenResolver(logger, builderStatus, accessToken, polarisHome, accessTokenFilePath, userHomePath);
        Optional<String> optionalAccessToken = accessTokenResolver.resolveAccessToken();
        if (!optionalAccessToken.isPresent()) {
            builderStatus.addErrorMessage("An access token must be resolvable from one of the following (this is also the order of precedence):");
            builderStatus.addErrorMessage(" - set explicitly");
            builderStatus.addErrorMessage(" - set from property (POLARIS_ACCESS_TOKEN, SWIP_ACCESS_TOKEN)");
            builderStatus.addErrorMessage(" - found in a provided file path (POLARIS_ACCESS_TOKEN_FILE, SWIP_ACCESS_TOKEN_FILE)");
            builderStatus.addErrorMessage(" - found in the '.access_token' file in a Polaris home directory (POLARIS_HOME, SWIP_HOME, or defaults to USER_HOME/.swip)");
        }

        if (timeoutSeconds <= 0) {
            builderStatus.addErrorMessage("A timeout (in seconds) greater than zero must be specified.");
        }

        CredentialsBuilder proxyCredentialsBuilder = new CredentialsBuilder();
        proxyCredentialsBuilder.setUsername(proxyUsername);
        proxyCredentialsBuilder.setPassword(proxyPassword);
        BuilderStatus proxyCredentialsBuilderStatus = proxyCredentialsBuilder.validateAndGetBuilderStatus();
        if (!proxyCredentialsBuilderStatus.isValid()) {
            builderStatus.addErrorMessage("The proxy credentials were not valid.");
            builderStatus.addAllErrorMessages(proxyCredentialsBuilderStatus.getErrorMessages());
        } else {
            Credentials proxyCredentials = proxyCredentialsBuilder.build();
            ProxyInfoBuilder proxyInfoBuilder = new ProxyInfoBuilder();
            proxyInfoBuilder.setCredentials(proxyCredentials);
            proxyInfoBuilder.setHost(proxyHost);
            proxyInfoBuilder.setPort(proxyPort);
            proxyInfoBuilder.setNtlmDomain(proxyNtlmDomain);
            proxyInfoBuilder.setNtlmWorkstation(proxyNtlmWorkstation);
            BuilderStatus proxyInfoBuilderStatus = proxyInfoBuilder.validateAndGetBuilderStatus();
            if (!proxyInfoBuilderStatus.isValid()) {
                builderStatus.addAllErrorMessages(proxyInfoBuilderStatus.getErrorMessages());
            }
        }
    }

    public List<String> getAllPropertyKeys() {
        List<String> allKeys = new ArrayList<>();
        for (Property property : Property.values()) {
            allKeys.addAll(property.getKeysInPriorityOrder());
        }

        return allKeys;
    }

    public PolarisServerConfigBuilder setFromProperties(Map<String, String> properties) {
        for (Property property : Property.values()) {
            property.setField(this, properties);
        }

        return this;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public PolarisServerConfigBuilder setLogger(IntLogger logger) {
        if (null != logger) {
            this.logger = logger;
        }
        return this;
    }

    public Gson getGson() {
        return gson;
    }

    public PolarisServerConfigBuilder setGson(Gson gson) {
        if (null != gson) {
            this.gson = gson;
        }
        return this;
    }

    public AuthenticationSupport getAuthenticationSupport() {
        return authenticationSupport;
    }

    public PolarisServerConfigBuilder setAuthenticationSupport(AuthenticationSupport authenticationSupport) {
        if (null != authenticationSupport) {
            this.authenticationSupport = authenticationSupport;
        }
        return this;
    }

    public enum Property {
        URL("polarisUrl", "SWIP_SERVER_URL"),
        ACCESS_TOKEN("accessToken", "SWIP_ACCESS_TOKEN"),
        TIMEOUT_IN_SECONDS("timeoutSeconds"),
        PROXY_HOST("proxyHost"),
        PROXY_PORT("proxyPort"),
        PROXY_USERNAME("proxyUsername"),
        PROXY_PASSWORD("proxyPassword"),
        PROXY_NTLM_DOMAIN("proxyNtlmDomain"),
        PROXY_NTLM_WORKSTATION("proxyNtlmWorkstation"),
        TRUST_CERT("trustCert"),
        HOME("polarisHome", "SWIP_HOME"),
        ACCESS_TOKEN_FILE("accessTokenFilePath", "SWIP_ACCESS_TOKEN_FILE"),
        USER_HOME("userHomePath", "USER_HOME");

        private final String fieldName;
        private String alternateName;

        Property(String fieldName) {
            this.fieldName = fieldName;
        }

        Property(String fieldName, String alternateName) {
            this(fieldName);
            this.alternateName = alternateName;
        }

        public List<String> getKeysInPriorityOrder() {
            String environmentKey = PolarisServerConfigBuilder.POLARIS_SERVER_CONFIG_ENVIRONMENT_VARIABLE_PREFIX + name();
            String propertyKey = environmentKey.toLowerCase().replace("_", ".");

            List<String> keys = new ArrayList<>();
            keys.add(propertyKey);
            keys.add(environmentKey);
            if (null != alternateName) {
                keys.add(alternateName.toLowerCase().replace("_", "."));
                keys.add(alternateName);
            }

            return keys;
        }

        public Optional<String> getValue(Map<String, String> properties) {
            List<String> keys = getKeysInPriorityOrder();
            return getValueFromKeysInPriorityOrder(keys, properties);
        }

        public void setField(PolarisServerConfigBuilder builder, Map<String, String> properties) {
            try {
                Optional<String> propertyValue = getValue(properties);
                if (!propertyValue.isPresent() || StringUtils.isBlank(propertyValue.get())) {
                    return;
                }

                Field builderField = builder.getClass().getDeclaredField(fieldName);
                builderField.setAccessible(true);
                Type builderFieldType = builderField.getType();
                if (String.class.equals(builderFieldType)) {
                    builderField.set(builder, propertyValue.get());
                } else if (Integer.TYPE.equals(builderFieldType)) {
                    int value = NumberUtils.toInt(propertyValue.get(), 0);
                    builderField.set(builder, value);
                } else if (Boolean.TYPE.equals(builderFieldType)) {
                    boolean value = Boolean.parseBoolean(propertyValue.get());
                    builderField.set(builder, value);
                }
            } catch (Exception ignored) {
                // ignored
            }
        }

        private Optional<String> getValueFromKeysInPriorityOrder(List<String> keysToLookFor, Map<String, String> properties) {
            for (String key : keysToLookFor) {
                if (properties.containsKey(key)) {
                    return Optional.ofNullable(properties.get(key));
                }
            }

            return Optional.empty();
        }
    }

    public String getPolarisUrl() {
        return polarisUrl;
    }

    public void setPolarisUrl(String polarisUrl) {
        this.polarisUrl = polarisUrl;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public boolean isTrustCert() {
        return trustCert;
    }

    public void setTrustCert(boolean trustCert) {
        this.trustCert = trustCert;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public String getProxyNtlmDomain() {
        return proxyNtlmDomain;
    }

    public void setProxyNtlmDomain(String proxyNtlmDomain) {
        this.proxyNtlmDomain = proxyNtlmDomain;
    }

    public String getProxyNtlmWorkstation() {
        return proxyNtlmWorkstation;
    }

    public void setProxyNtlmWorkstation(String proxyNtlmWorkstation) {
        this.proxyNtlmWorkstation = proxyNtlmWorkstation;
    }

    public String getPolarisHome() {
        return polarisHome;
    }

    public void setPolarisHome(String polarisHome) {
        this.polarisHome = polarisHome;
    }

    public String getAccessTokenFilePath() {
        return accessTokenFilePath;
    }

    public void setAccessTokenFilePath(String accessTokenFilePath) {
        this.accessTokenFilePath = accessTokenFilePath;
    }

    public String getUserHomePath() {
        return userHomePath;
    }

    public void setUserHomePath(String userHomePath) {
        this.userHomePath = userHomePath;
    }

}
