package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerProjectDashboardResponse {

    // Professional project details
    private ProjectResponse projectDetails;

    // Test Case metrics for this specific project
    private ManagerDashboardOverviewResponse.TestCaseMetrics testCaseStats;

    // Bug metrics for this specific project
    private ManagerDashboardOverviewResponse.BugMetrics bugStats;

    // Quality health score for this project
    private QualityHealthDto qualityHealth;

    // Features under this project with comprehensive test & bug breakdown
    private List<ManagerFeatureDetailMetrics> features;

    // Recent bugs in this project
    private List<ManagerRecentBugDto> recentBugs;

    // Active testers and contributors in this project
    private List<String> activeContributors;
}
