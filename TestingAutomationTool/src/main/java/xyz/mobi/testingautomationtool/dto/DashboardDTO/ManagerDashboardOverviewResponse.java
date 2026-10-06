package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboardOverviewResponse {

    // Project summary counts
    private ProjectCounts projectStats;

    // Feature summary counts
    private FeatureCounts featureStats;

    // Test case overall metrics
    private TestCaseMetrics testCaseStats;

    // Bug overall metrics
    private BugMetrics bugStats;

    // Executive Quality & Readiness Health
    private QualityHealthDto qualityHealth;

    // List of projects with high-level stats
    private List<ManagerProjectSummaryDto> projects;

    // Top recent defects across all projects
    private List<ManagerRecentBugDto> recentBugs;

    // Top failing features / Blockers
    private List<ManagerFeatureDetailMetrics> topFailingFeatures;

    // Team Stats
    private TeamMetrics teamStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectCounts {
        private Long totalProjects;
        private Long activeProjects;
        private Long inactiveProjects;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeatureCounts {
        private Long totalFeatures;
        private Long activeFeatures;
        private Long inProgressFeatures;
        private Long completedFeatures;
        private Long onHoldFeatures;
        private Long deprecatedFeatures;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestCaseMetrics {
        private Long totalTestCases;
        private Long passedCount;
        private Long failedCount;
        private Long noRunCount;
        private Long descopeCount;
        private Double passRatePercentage;
        private Double executionRatePercentage;
        private Map<String, Long> testTypeBreakdown;
        private Map<String, Long> testPriorityBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BugMetrics {
        private Long totalBugs;
        private Long openBugs;
        private Long inProgressBugs;
        private Long resolvedBugs;
        private Long closedBugs;
        private Long criticalBugs;
        private Long highBugs;
        private Long mediumBugs;
        private Long lowBugs;
        private Map<String, Long> severityBreakdown;
        private Map<String, Long> priorityBreakdown;
        private Map<String, Long> categoryBreakdown;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamMetrics {
        private Long totalActiveUsers;
        private Long testersCount;
        private Long managersCount;
        private Long adminsCount;
    }
}
