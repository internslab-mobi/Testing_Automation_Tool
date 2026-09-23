package xyz.mobi.testingautomationtool.dto.TestingExecutionDTO;

import jakarta.validation.constraints.Positive;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestingExecutionRequest {

//    @NotNull(message = "Test case ID is required")
//    @Positive(message = "Test case ID must be greater than 0")
//    private TestCase testcase;

//    @NotNull(message = "Bugs count is required")
//    @Min(value = 0, message = "Bugs count cannot be negative")
//    private Integer bugsCount;

//    @NotNull(message = "Execution number is required")
//    @Min(value = 0, message = "Execution number must be greater than 0")
//    private Integer executionNumber;

    private AutomationFeasibility automationFeasibility;

//    private ExecutionStatus executionStatus;

    private String testExecution;
    private String testValidation;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String comments;

//    @NotNull(message = "Executed by is required")
    @Positive(message = "Executed by must be greater than 0")
    private Integer executedBy;
}