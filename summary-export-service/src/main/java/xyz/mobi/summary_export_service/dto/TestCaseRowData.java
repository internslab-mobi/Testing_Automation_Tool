package xyz.mobi.summary_export_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseRowData {
    private Integer testcaseId;
    private String testcaseFormatId;
    private String featureName;
    private String title;
    private String testType;
    private String testExecution;
    private String testValidation;
    private String automationPriority;
    private String testPriority;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String uiValidation;
    private String dbValidation;
    private String automationStatus;
    private String actualStatus;
    private String executionStatus;
    private String comments;
    private Instant executedAt;
}
