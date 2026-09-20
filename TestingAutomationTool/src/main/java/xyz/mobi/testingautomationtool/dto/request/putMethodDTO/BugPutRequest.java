package xyz.mobi.testingautomationtool.dto.request.putMethodDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    private String description;

    @NotNull(message = "Priority is required")
    private BugPriority priority;

    @NotNull(message = "Severity is required")
    private BugSeverity severity;

    @NotNull(message = "Status is required")
    private BugStatus status;
}
