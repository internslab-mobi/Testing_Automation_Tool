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

    private Integer bugId;
    private String bugFormatId;
    private String title;
    private String description;

    private Integer testcaseId;
    private String testcaseFormatId;

    private Integer featureId;
    private String featureName;

    private BugSeverity severity;
    private BugPriority priority;
    private BugCategory category;
    private BugStatus status;

    private String reportedBy;
    private String assignedTo;
    private String executedBy;
    private String updatedBy;

    private Instant resolvedAt;
    private Integer bugOccurrence;

    private String comments;
    private Map<String, Object> dynamicFields;

    private Instant createdAt;
    private Instant updatedAt;

    private Boolean isActive;
    private Boolean isDeleted;
}
