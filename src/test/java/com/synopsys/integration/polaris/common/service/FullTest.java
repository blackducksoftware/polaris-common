package com.synopsys.integration.polaris.common.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonPath;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.polaris.common.api.PolarisComponent;
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

        Optional<ProjectV0> project = projectService.getProjectByName("common");
        System.out.println(project.get().getId());

        List<BranchV0> allBranches = branchService.getBranchesForProject("0816bc95-e1e6-491a-a5b5-f01af25e6983");
        allBranches.stream().forEach(System.out::println);

        Optional<BranchV0> branch = branchService.getBranchForProjectByName("0816bc95-e1e6-491a-a5b5-f01af25e6983", "master");
        System.out.println(branch.get().getId());

        List<QueryIssue> queryIssues = issueService.getIssuesForProjectAndBranch("0816bc95-e1e6-491a-a5b5-f01af25e6983", "f41fcb75-49a5-4b6b-882d-583bd79586b9");
        queryIssues.stream().forEach(System.out::println);
        List<String> issueKeys = queryIssues.stream().map(queryIssue -> queryIssue.getAttributes().getIssueKey()).collect(Collectors.toList());

        for (String issueKey : issueKeys) {
            Issue issue = issueService.getIssueForProjectBranchAndIssueKey("0816bc95-e1e6-491a-a5b5-f01af25e6983", "f41fcb75-49a5-4b6b-882d-583bd79586b9", issueKey);
            System.out.println(issue.getLabel() + " " + issue.getSourcePath());
        }
    }

}
