package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCasePutRequest {

    @Size(max = 500, message = "Title cannot exceed 500 characters")
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

    private Map<String, Object> dynamicFields;
}
