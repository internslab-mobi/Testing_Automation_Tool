package xyz.mobi.testingautomationtool.dto.BugDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugRequest {

    @NotBlank
    @Size(max = 255)
    private String bugFormatId;

    @NotNull
    private Integer testCaseId;

    private Integer featureId;

    @NotBlank
    @Size(max = 300)
    private String title;

    private String description;

    @NotNull
    private BugSeverity severity;

    @NotNull
    private BugPriority priority;

    @NotNull
    private Integer reportedBy;

    private BugStatus status;
    private Integer assignedTo;
    private Integer bugReoccurredId;
    private Integer bugOccurrence;
}