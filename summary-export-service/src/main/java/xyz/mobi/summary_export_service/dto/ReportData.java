package xyz.mobi.summary_export_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportData {

    private String reportScope; // "PROJECT" or "FEATURE"
    private Integer targetId;
    private String targetName;
    private String projectName;
    private String region;
    private String status;
    private String description;
    private String exportDate;

    // KPI Metrics
    private int totalTestCases;
    private int executedCount;
    private double executionRate;
    private int passedCount;
    private int failedCount;
    private int noRunCount;
    private int blockedCount;
    private double passRate;

    // Defect Metrics
    private int totalDefects;
    private int criticalDefects;
    private int openDefects;
    private int closedDefects;
    private double defectFixRate;

    // Health Score
    private int qualityHealthScore;
    private String healthRating;

    @Builder.Default
    private List<TestCaseRowData> testCaseRows = new ArrayList<>();

    @Builder.Default
    private List<BugRowData> bugRows = new ArrayList<>();

    @Builder.Default
    private List<FeatureSummaryRowData> featureRows = new ArrayList<>();
}
