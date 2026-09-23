package xyz.mobi.testingautomationtool.dto.request.postMethodDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;
import xyz.mobi.testingautomationtool.enums.ExecutionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutionRequest {

    private AutomationFeasibility automationFeasibility;

    private ExecutionStatus executionStatus;

    @Size(max = 65535, message = "Test execution cannot exceed the allowed length")
    private String testExecution;

    @Size(max = 65535, message = "Test validation cannot exceed the allowed length")
    private String testValidation;

    @Size(max = 65535, message = "UI validations cannot exceed the allowed length")
    private String uiValidations;

    @Size(max = 65535, message = "DB validations cannot exceed the allowed length")
    private String dbValidations;

    @Size(max = 65535, message = "Comments cannot exceed the allowed length")
    private String comments;

    @Size(max = 65535, message = "Precondition cannot exceed the allowed length")
    private String precondition;

    @Size(max = 65535, message = "Execution steps cannot exceed the allowed length")
    private String executionSteps;

    @Size(max = 65535, message = "Test data cannot exceed the allowed length")
    private String testData;
}
