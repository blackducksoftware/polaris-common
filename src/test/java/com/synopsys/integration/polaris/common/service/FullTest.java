package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.generated.common.BranchV0;
import com.synopsys.integration.polaris.common.api.generated.common.ProjectV0;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfig;
import com.synopsys.integration.polaris.common.configuration.PolarisServerConfigBuilder;
import com.synopsys.integration.polaris.common.model.Issue;
import com.synopsys.integration.polaris.common.model.QueryIssue;

public class FullTest {
    public static void main(String[] args) throws IntegrationException {
        PolarisServerConfigBuilder polarisServerConfigBuilder = PolarisServerConfig.newBuilder();
        polarisServerConfigBuilder.setUrl(System.getenv("POLARIS_URL"));
        polarisServerConfigBuilder.setAccessToken(System.getenv("POLARIS_ACCESS_TOKEN"));

        PolarisServerConfig polarisServerConfig = polarisServerConfigBuilder.build();
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        PolarisServicesFactory polarisServicesFactory = polarisServerConfig.createPolarisServicesFactory(logger);

        ProjectService projectService = polarisServicesFactory.createProjectService();
        BranchService branchService = polarisServicesFactory.createBranchService();
        IssueService issueService = polarisServicesFactory.createIssueService();

        List<ProjectV0> allProjects = projectService.getAllProjects();
        allProjects.stream().forEach(System.out::println);

        Optional<ProjectV0> project = projectService.getProjectByName("integration-common");
        System.out.println(project.get().getId());

        Optional<BranchV0> branch = branchService.getBranchForProjectByName(project.get().getId(), "17.0.1-SNAPSHOT");
        System.out.println(branch.get().getId());

        List<QueryIssue> queryIssues = issueService.getIssuesForProjectAndBranch(project.get().getId(), branch.get().getId());
        queryIssues.stream().forEach(System.out::println);
        List<String> issueKeys = queryIssues.stream().map(queryIssue -> queryIssue.getAttributes().getIssueKey()).collect(Collectors.toList());

        for (String issueKey : issueKeys) {
            Issue issue = issueService.getIssueForProjectBranchAndIssueKey(project.get().getId(), branch.get().getId(), issueKey);
            System.out.println(issue.getLabel() + " " + issue.getSourcePath());
        }
    }

}
