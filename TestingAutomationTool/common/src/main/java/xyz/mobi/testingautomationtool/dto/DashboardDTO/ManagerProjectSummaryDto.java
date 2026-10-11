package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerProjectSummaryDto {
    private Integer projectId;
    private String projectName;
    private String description;
    private String region;
    private ProjectStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private String createdByName;

    // Counts & Rates
    private Long featureCount;
    private Long testCaseCount;
    private Long passedCount;
    private Long failedCount;
    private Long noRunCount;
    private Long descopeCount;
    private Double passRatePercentage;
    private Double executionRatePercentage;

    private Long bugCount;
    private Long openBugCount;
    private Long criticalBugCount;

    private String healthStatus;
    private Double healthScore;
}
