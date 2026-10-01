package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatchExecutionResponse {
    private String comments;
    private ExecutionStatus executionStatus;
    private AutomationFeasibility automationFeasibility;
}
