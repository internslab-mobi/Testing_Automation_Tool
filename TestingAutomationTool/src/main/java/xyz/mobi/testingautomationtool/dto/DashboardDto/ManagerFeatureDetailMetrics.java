package xyz.mobi.testingautomationtool.dto.DashboardDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerFeatureDetailMetrics {
    private Integer featureId;
    private Integer projectId;
    private String projectName;
    private String featureName;
    private String description;
    private FeatureStatus status;
    private Integer sprint;
    private String featureVersion;
    private Long duration;
    private Instant startTime;
    private Integer createdBy;
    private String creatorName;
    private Instant createdAt;
    private Instant updatedAt;

    // Test Case Statistics
    private Long totalTestCases;
    private Long passedCount;
    private Long failedCount;
    private Long noRunCount;
    private Long descopeCount;
    private Double passRatePercentage;
    private Double executionRatePercentage;

    // Bug Statistics
    private Long totalBugs;
    private Long openBugs;
    private Long inProgressBugs;
    private Long resolvedBugs;
    private Long closedBugs;
    private Long criticalBugs;
    private Long highBugs;
    private Long mediumBugs;
    private Long lowBugs;

    // Health Assessment
    private String healthStatus; // "HEALTHY", "WARNING", "CRITICAL"
    private Double healthScore;
}
