package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.common.branch.BranchV0Resource;
import com.synopsys.integration.polaris.common.api.common.project.ProjectV0Resource;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.model.Issue;
import com.synopsys.integration.polaris.common.model.QueryIssueResource;

public class FullTest {
    public static void main(final String[] args) throws IntegrationException {
        final PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setUrl(System.getenv("POLARIS_URL"));
        polarisServerConfigBuilder.setAccessToken(System.getenv("POLARIS_ACCESS_TOKEN"));

        final PolarisServerConfig polarisServerConfig = polarisServerConfigBuilder.build();
        final IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);

        final ProjectService projectService = polarisServicesFactory.createProjectService();
        final BranchService branchService = polarisServicesFactory.createBranchService();
        final IssueService issueService = polarisServicesFactory.createIssueService();

        final List<ProjectV0Resource> allProjects = projectService.getAllProjects();
        allProjects.stream().forEach(System.out::println);

        final Optional<ProjectV0Resource> project = projectService.getProjectByName("integration-common");
        System.out.println(project.get().getId());

        final Optional<BranchV0Resource> branch = branchService.getBranchForProjectByName(project.get().getId(), "17.0.1-SNAPSHOT");
        System.out.println(branch.get().getId());

        final List<QueryIssueResource> queryIssues = issueService.getIssuesForProjectAndBranch(project.get().getId(), branch.get().getId());
        queryIssues.stream().forEach(System.out::println);
        final List<String> issueKeys = queryIssues.stream().map(queryIssue -> queryIssue.getAttributes().getIssueKey()).collect(Collectors.toList());

        for (final String issueKey : issueKeys) {
            final Issue issue = issueService.getIssueForProjectBranchAndIssueKey(project.get().getId(), branch.get().getId(), issueKey);
            System.out.println(issue.getLabel() + " " + issue.getSourcePath());
        }
    }

}
