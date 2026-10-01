package xyz.mobi.testingautomationtool.dto.BugDto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BugPutRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private BugPriority priority;

    private BugSeverity severity;

    private BugStatus status;
}
