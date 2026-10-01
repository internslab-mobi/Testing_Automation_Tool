package xyz.mobi.testingautomationtool.dto.BugDto;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugResponse {

    private Integer bugId;
    private String bugFormatId;
    private Integer testcaseId;
    private Integer featureId;
    private String title;
    private String description;
    private BugSeverity severity;
    private BugPriority priority;
    private BugCategory category;
    private BugStatus status;
    private String reportedBy;
    private String assignedTo;
    private String executedBy;
    private String updatedBy;
    private Instant resolvedAt;
    private Integer bugReoccurredId;
    private Integer bugOccurrence;
    private String comments;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean active;
}
