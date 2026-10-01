package xyz.mobi.testingautomationtool.dto.TestCaseDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResponse {

    private Integer testcaseId;
    private String testcaseFormatId;
    private Integer featureId;
    private String title;
    private TestType testType;
    private TestPriority testPriority;
    private TestCaseStatus testcaseStatus;
    private ExecutionStatus executionStatus;
    private AutomationFeasibility automationFeasibility;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String comments;
    private String createdBy;
    private String updatedBy;
    private Boolean isActive;
    private Boolean isDeleted;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private Map<String, Object> dynamicFields;

    // Structured fields for composite responses
    private TestCaseDetails testCase;
    private ExecutionResponse execution;
    private ValidationResponse validations;
    private AutomationResponse automation;
    private BugSummaryResponse bugSummary;
    private AuditResponse audit;
}