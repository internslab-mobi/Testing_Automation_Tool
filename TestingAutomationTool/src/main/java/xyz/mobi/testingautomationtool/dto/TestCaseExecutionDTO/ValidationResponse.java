package xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {

    private String testValidation;
    private String uiValidations;
    private String dbValidations;
}