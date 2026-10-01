package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateExecutionStatusRequest {
    private ExecutionStatus executionStatus;
    private AutomationFeasibility automationFeasibility;
    private String comments;
}
