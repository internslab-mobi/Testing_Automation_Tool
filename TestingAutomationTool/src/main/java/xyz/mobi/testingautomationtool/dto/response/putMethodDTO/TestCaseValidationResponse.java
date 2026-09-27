package xyz.mobi.testingautomationtool.dto.response.putMethodDTO;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
