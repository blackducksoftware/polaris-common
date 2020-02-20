/**
 * polaris-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.polaris.common.service;

import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.api.job.model.Job;
import com.synopsys.integration.polaris.common.api.job.model.JobAttributes;
import com.synopsys.integration.polaris.common.api.job.model.JobResource;
import com.synopsys.integration.polaris.common.api.job.model.JobStatus;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.wait.WaitJob;

public class JobService {
    private static final String JOB_SERVICE_API_SPEC = "/api/jobs";
    private static final String JOBS_API_SPEC = JOB_SERVICE_API_SPEC + "/jobs";
    private static final TypeToken JOB_RESOURCE = new TypeToken<JobResource>() {};
    private final IntLogger logger;
    private final AccessTokenPolarisHttpClient polarisHttpClient;
    private final PolarisService polarisService;

    public JobService(final AccessTokenPolarisHttpClient polarisHttpClient, final PolarisService polarisService) {
        this.logger = polarisHttpClient.getLogger();
        this.polarisHttpClient = polarisHttpClient;
        this.polarisService = polarisService;
    }

    public JobResource getJobById(final String jobId) throws IntegrationException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + JOBS_API_SPEC + "/" + jobId;
        return getJobByUrl(uri);
    }

    public JobResource getJobByUrl(final String jobApiUrl) throws IntegrationException {
        final Request request = PolarisRequestFactory.createDefaultRequestBuilder().uri(jobApiUrl).build();
        return polarisService.get(JOB_RESOURCE.getType(), request);
    }

    public boolean waitForJobToCompleteById(final String jobId) throws IntegrationException, InterruptedException {
        return waitForJobToCompleteById(jobId, polarisHttpClient.getTimeoutInSeconds());
    }

    public boolean waitForJobToCompleteById(final String jobId, final int timeoutInSeconds) throws IntegrationException, InterruptedException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + JOBS_API_SPEC + "/" + jobId;
        return waitForJobToCompleteByUrl(uri, timeoutInSeconds);
    }

    public boolean waitForJobToCompleteByUrl(final String jobApiUrl) throws IntegrationException, InterruptedException {
        return waitForJobToCompleteByUrl(jobApiUrl, polarisHttpClient.getTimeoutInSeconds());
    }

    public boolean waitForJobToCompleteByUrl(final String jobApiUrl, final int timeoutInSeconds) throws IntegrationException, InterruptedException {
        WaitJob waitJob = WaitJob.createUsingSystemTimeWhenInvoked(logger, timeoutInSeconds, 2, () -> hasJobCompleted(jobApiUrl));
        return waitJob.waitFor();
    }

    private boolean hasJobCompleted(final String jobApiUrl) throws IntegrationException {
        try {
            final JobStatus jobStatus = Optional.ofNullable(getJobByUrl(jobApiUrl))
                                            .map(JobResource::getData)
                                            .map(Job::getAttributes)
                                            .map(JobAttributes::getStatus)
                                            .orElse(null);

            if (jobStatus == null) {
                logger.alwaysLog("Job at url " + jobApiUrl + " exists but no status could be determined.");
                return false;
            }

            final JobStatus.StateEnum stateEnum = jobStatus.getState();
            if (JobStatus.StateEnum.QUEUED.equals(stateEnum) || JobStatus.StateEnum.RUNNING.equals(stateEnum) || JobStatus.StateEnum.DISPATCHED.equals(stateEnum)) {
                logger.alwaysLog("Job at url " + jobApiUrl + " exists with status " + stateEnum.toString() + ". Progress: " + jobStatus.getProgress());
                return false;
            }

        } catch (final IntegrationException e) {
            if (e.getMessage().contains("404")) {
                logger.debug("Job at url " + jobApiUrl + " could not be found. Retrying...");
            } else {
                throw e;
            }
        }

        return true;
    }

}