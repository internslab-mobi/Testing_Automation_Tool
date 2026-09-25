package xyz.mobi.testingautomationtool.dto.request.putMethodDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class TestCasePutRequest {

    @NotBlank(message = "Title should not be empty")
    @Size(max = 300)
    private String title;

    @NotNull(message = "Test type is required")
    private TestType testType;

    @NotNull(message = "Test priority is required")
    private TestPriority testPriority;

    @NotBlank(message = "Test execution cannot be empty")
    private String testExecution;

    @NotBlank(message = "Test validation cannot be empty")
    private String testValidation;

    @NotBlank(message = "precondition cannot be empty")
    private String precondition;

    @NotBlank(message = "Test Data cannot be empty")
    private String testData;

    @NotBlank(message = "Test execution steps cannot be empty")
    private String executionSteps;

    @NotBlank(message = "Ui validation cannot be empty")
    private String uiValidations;

    @NotBlank(message = "Db validation cannot be empty")
    private String dbValidations;

    @NotBlank(message = "Test comments cannot be empty")
    private String comments;

    private java.util.Map<String, Object> dynamicFields;
}
