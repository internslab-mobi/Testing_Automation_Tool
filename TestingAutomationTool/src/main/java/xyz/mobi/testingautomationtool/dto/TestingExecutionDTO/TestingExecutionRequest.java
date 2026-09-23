package xyz.mobi.testingautomationtool.dto.TestingExecutionDTO;

import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecutionRequest {

    @NotNull
    private Integer testcaseId;

    @Min(value = 0, message = "Bugs count cannot be negative")
    private Integer bugsCount;

    private AutomationFeasibility automationFeasibility;

    private String testExecution;

    private String testValidation;

    private String precondition;

    private String testData;

    private String executionSteps;

    private String uiValidations;

    private String dbValidations;

    private String comments;

    @NotNull
    private Integer executedBy;
}
