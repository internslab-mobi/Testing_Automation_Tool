package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExecutionStatusRequest {

    @NotNull(message = "Execution status is required")
    private ExecutionStatus executionStatus;

    private String bugTitle;

    private String bugDescription;

    private BugSeverity bugSeverity;

    private BugPriority bugPriority;

    private BugStatus bugStatus;
}