package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationResponse {

    private AutomationFeasibility automationFeasibility;
}