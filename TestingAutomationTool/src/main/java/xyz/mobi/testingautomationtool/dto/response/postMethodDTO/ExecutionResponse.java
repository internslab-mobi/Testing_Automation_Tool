package xyz.mobi.testingautomationtool.dto.TestingExecutionDTO;

import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionResponse {

    private Integer testcaseId;

    private Integer bugsCount;

    private Integer executionNumber;

    private AutomationFeasibility automationFeasibility;

    private ValidationResponse validations;

}
