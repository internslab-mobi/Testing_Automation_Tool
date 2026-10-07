package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCasePutResponse {

    // --- Legacy Flat Fields (Commented to prevent duplicate keys in JSON response) ---
    // private Integer testcaseId;
    // private Integer featureId;
    // private String title;
    // private TestType testType;
    // private TestPriority testPriority;
    // private String username;
    // private Instant updatedAt;

    // Structured inner JSON fields (using static inner classes)
    private TestCaseDetails testCase;
    private ValidationResponse validation;
    private AuditResponse audit;
    private Map<String, Object> dynamicFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestCaseDetails {
        private Integer testcaseId;
        private Integer featureId;
        private String title;
        private TestType testType;
        private TestPriority testPriority;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationResponse {
        private String testExecution;
        private String testValidation;
        private String precondition;
        private String testData;
        private String executionSteps;
        private String uiValidations;
        private String dbValidations;
        private String comments;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditResponse {
        private String updatedBy;
        private Instant updatedAt;
    }
}
