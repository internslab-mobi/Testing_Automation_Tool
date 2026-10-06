package xyz.mobi.testingautomationtool.dto.TestCaseDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseValidationResponse {
    private String testExecution;
    private String testValidation;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private String comments;
}
