package xyz.mobi.summary_export_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureSummaryRowData {
    private Integer featureId;
    private String featureName;
    private String status;
    private int totalTestCases;
    private int passedCount;
    private int failedCount;
    private int bugCount;
}
