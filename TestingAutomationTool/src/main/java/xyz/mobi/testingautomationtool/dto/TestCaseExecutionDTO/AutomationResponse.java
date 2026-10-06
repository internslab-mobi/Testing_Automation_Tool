package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutomationResponse {
    private AutomationFeasibility automationFeasibility;
}