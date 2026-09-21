package xyz.mobi.testingautomationtool.dto.request.patchmethodDTO;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExecutionStatusRequest {

    @NotNull(message = "Execution status is required")
    private ExecutionStatus executionStatus;

    private AutomationFeasibility automationFeasibility;
}