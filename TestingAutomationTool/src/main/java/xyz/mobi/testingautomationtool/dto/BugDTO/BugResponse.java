package xyz.mobi.testingautomationtool.dto.BugDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugResponse {

    private Integer bugId;
    private String bugFormatId;
    private Integer testCaseId;
    private Integer featureId;
    private String title;
    private String description;
    private BugSeverity severity;
    private BugPriority priority;
    private BugStatus status;
    private Integer reportedBy;
    private Integer assignedTo;
    private Integer bugOccurrence;
}
