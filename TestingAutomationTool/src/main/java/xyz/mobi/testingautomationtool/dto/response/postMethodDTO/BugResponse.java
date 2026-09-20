package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

import java.time.LocalDateTime;

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

    private BugStatus status;

    private Integer reportedBy;

    private Integer assignedTo;

    private LocalDateTime resolvedAt;

    private Integer bugReoccurredId;

    private Integer bugOccurrence;

    private boolean active;
}