package xyz.mobi.testingautomationtool.dto.BugDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BugResponse {

    // --- Legacy Flat Fields (Commented to prevent duplicate keys in JSON response) ---
    // private Integer bugId;
    // private String bugFormatId;
    // private String title;
    // private String description;
    // private Integer testcaseId;
    // private String testcaseFormatId;
    // private Integer featureId;
    // private String featureName;
    // private BugSeverity severity;
    // private BugPriority priority;
    // private BugCategory category;
    // private BugStatus status;
    // private String reportedBy;
    // private String assignedTo;
    // private String executedBy;
    // private String updatedBy;
    // private Instant resolvedAt;
    // private Integer bugOccurrence;
    // private String comments;
    // private Instant createdAt;
    // private Instant updatedAt;
    // private Boolean isActive;
    // private Boolean isDeleted;

    // Structured inner JSON fields (using static inner classes)
    private BugDetails bug;
    private TestCaseRef testCase;
    private FeatureRef feature;
    private WorkflowInfo workflow;
    private AuditResponse audit;
    private Map<String, Object> dynamicFields;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BugDetails {
        private Integer bugId;
        private String bugFormatId;
        private String title;
        private String description;
        private BugSeverity severity;
        private BugPriority priority;
        private BugCategory category;
        private BugStatus status;
        private Integer bugOccurrence;
        private String comments;
        private Boolean isActive;
        private Boolean isDeleted;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TestCaseRef {
        private Integer testcaseId;
        private String testcaseFormatId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FeatureRef {
        private Integer featureId;
        private String featureName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WorkflowInfo {
        private String reportedBy;
        private String assignedTo;
        private String executedBy;
        private Instant resolvedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuditResponse {
        private String updatedBy;
        private Instant createdAt;
        private Instant updatedAt;
    }
}
