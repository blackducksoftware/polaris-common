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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import com.google.gson.reflect.TypeToken;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.polaris.common.api.job.model.FailureInfo;
import com.synopsys.integration.polaris.common.api.job.model.Job;
import com.synopsys.integration.polaris.common.api.job.model.JobAttributes;
import com.synopsys.integration.polaris.common.api.job.model.JobResource;
import com.synopsys.integration.polaris.common.api.job.model.JobStatus;
import com.synopsys.integration.polaris.common.exception.PolarisIntegrationException;
import com.synopsys.integration.polaris.common.request.PolarisRequestFactory;
import com.synopsys.integration.polaris.common.rest.AccessTokenPolarisHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.wait.WaitJob;

public class JobService {
    public static final long DEFAULT_TIMEOUT = 30 * 60L;
    public static final int DEFAULT_WAIT_INTERVAL = 5;

    private static final String JOB_SERVICE_API_SPEC = "/api/jobs";
    private static final String JOBS_API_SPEC = JOB_SERVICE_API_SPEC + "/jobs";
    private static final TypeToken<JobResource> JOB_RESOURCE = new TypeToken<JobResource>() {};
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

    public void waitForJobStateIsCompletedOrDieById(final String jobId) throws IntegrationException, InterruptedException {
        waitForJobStateIsCompletedOrDieById(jobId, polarisHttpClient.getTimeoutInSeconds(), DEFAULT_WAIT_INTERVAL);
    }

    public void waitForJobStateIsCompletedOrDieById(final String jobId, final long timeoutInSeconds, final int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        final String uri = polarisHttpClient.getPolarisServerUrl() + JOBS_API_SPEC + "/" + jobId;
        waitForJobStateIsCompletedOrDieByUrl(uri, timeoutInSeconds, waitIntervalInSeconds);
    }

    public void waitForJobStateIsCompletedOrDieByUrl(final String jobApiUrl) throws IntegrationException, InterruptedException {
        waitForJobStateIsCompletedOrDieByUrl(jobApiUrl, polarisHttpClient.getTimeoutInSeconds(), DEFAULT_WAIT_INTERVAL);
    }

    public void waitForJobStateIsCompletedOrDieByUrl(final String jobApiUrl, final long timeoutInSeconds, final int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        WaitJob waitJob = WaitJob.createUsingSystemTimeWhenInvoked(logger, timeoutInSeconds, waitIntervalInSeconds, () -> hasJobEnded(jobApiUrl));
        if (!waitJob.waitFor()) {
            final String maximumDurationString = DurationFormatUtils.formatDurationHMS(waitIntervalInSeconds * 1000);
            throw new PolarisIntegrationException(String.format("Job at url %s did not end in the provided timeout of %s", jobApiUrl, maximumDurationString));
        }

        final JobResource jobResource = this.getJobByUrl(jobApiUrl);
        final JobStatus.StateEnum jobState = Optional.ofNullable(jobResource)
                                                 .map(JobResource::getData)
                                                 .map(Job::getAttributes)
                                                 .map(JobAttributes::getStatus)
                                                 .map(JobStatus::getState)
                                                 .orElseThrow(() -> new PolarisIntegrationException(String.format("Job at url %s ended but its state cannot be determined.", jobApiUrl)));

        if (!JobStatus.StateEnum.COMPLETED.equals(jobState)) {
            final StringBuilder errorMessageBuilder = new StringBuilder();
            errorMessageBuilder.append(String.format("Job at url %s ended with state %s instead of %s", jobApiUrl, jobState, JobStatus.StateEnum.COMPLETED));
            if (JobStatus.StateEnum.FAILED.equals(jobState)) {
                // Niether Data nor Attributes can be null because they were validated above -- rotte MAR 2020
                final FailureInfo failureInfo = jobResource.getData().getAttributes().getFailureInfo();
                if (failureInfo != null && StringUtils.isNotBlank(failureInfo.getUserFriendlyFailureReason())) {
                    errorMessageBuilder.append(String.format(" because: %s", failureInfo.getUserFriendlyFailureReason()));
                }
            }
            errorMessageBuilder.append("\r\nCheck the job status in Polaris for more details.");

            throw new PolarisIntegrationException(errorMessageBuilder.toString());
        }
    }

    private boolean hasJobEnded(final String jobApiUrl) throws IntegrationException {
        final String jobStatusPrefix = "Job at url " + jobApiUrl;

        try {
            final Optional<JobStatus> optionalJobStatus = Optional.ofNullable(getJobByUrl(jobApiUrl))
                                                              .map(JobResource::getData)
                                                              .map(Job::getAttributes)
                                                              .map(JobAttributes::getStatus);

            if (!optionalJobStatus.isPresent()) {
                logger.info(jobStatusPrefix + " was found but the job status could not be determined.");
                return false;
            }

            final JobStatus jobStatus = optionalJobStatus.get();
            final JobStatus.StateEnum stateEnum = jobStatus.getState();
            if (JobStatus.StateEnum.QUEUED.equals(stateEnum) || JobStatus.StateEnum.RUNNING.equals(stateEnum) || JobStatus.StateEnum.DISPATCHED.equals(stateEnum)) {
                logger.info(jobStatusPrefix + " was found with status " + stateEnum.toString() + ". Progress: " + jobStatus.getProgress());
                return false;
            }

        } catch (final IntegrationException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                logger.info(jobStatusPrefix + " could not be found.");
            } else {
                throw e;
            }
        }

        return true;
    }

}
