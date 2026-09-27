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

    @Size(max = 300)
    private String title;

    private TestType testType;

    private TestPriority testPriority;

    private String testExecution;

    private String testValidation;

    private String precondition;

    private String testData;

    private String executionSteps;

    private String uiValidations;

    private String dbValidations;

    private String comments;

    private java.util.Map<String, Object> dynamicFields;
}
