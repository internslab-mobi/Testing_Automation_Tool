package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    // --- Legacy Flat Fields (Commented to prevent duplicate keys in JSON response) ---
    // private Integer testcaseId;
    // private String testcaseFormatId;
    // private Integer featureId;
    // private String title;
    // private TestType testType;
    // private TestPriority testPriority;
    // private TestCaseStatus testcaseStatus;
    // private ExecutionStatus executionStatus;
    // private AutomationFeasibility automationFeasibility;
    // private String precondition;
    // private String testData;
    // private String executionSteps;
    // private String uiValidations;
    // private String dbValidations;
    // private String comments;
    // private String createdBy;
    // private String updatedBy;
    // private Boolean isActive;
    // private Boolean isDeleted;
    // private Long version;
    // private Instant createdAt;
    // private Instant updatedAt;

    // Structured inner JSON fields (using static inner classes)
    private TestCaseDetails testCase;
    private ExecutionResponse execution;
    private ValidationResponse validations;
    private AutomationResponse automation;
    private BugSummaryResponse bugSummary;
    private AuditResponse audit;
    private Map<String, Object> dynamicFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestCaseDetails {
        private Integer testcaseId;
        private String testcaseFormatId;
        private Integer featureId;
        private String title;
        private TestType testType;
        private TestPriority testPriority;
        private TestCaseStatus testcaseStatus;
        private Boolean isActive;
        private Boolean isDeleted;
        private Long version;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ExecutionResponse {
        private Integer executionId;
        private ExecutionStatus executionStatus;
        private String testExecution;
        private String precondition;
        private String testData;
        private String executionSteps;
        private String comments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationResponse {
        private String testValidation;
        private String uiValidations;
        private String dbValidations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AutomationResponse {
        private AutomationFeasibility automationFeasibility;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BugSummaryResponse {
        private Integer bugsCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditResponse {
        private String createdBy;
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}