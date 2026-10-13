package xyz.mobi.testingautomationtool.dto.TestingExecutionDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecutionRequest {
    private AutomationFeasibility automationFeasibility;
    private String testExecution;
    private String testValidation;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String comments;
}