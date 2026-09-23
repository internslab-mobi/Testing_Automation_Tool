package xyz.mobi.testingautomationtool.dto.TestcaseDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.TestCaseStatus;
import xyz.mobi.testingautomationtool.enums.TestPriority;
import xyz.mobi.testingautomationtool.enums.TestType;
import xyz.mobi.testingautomationtool.enums.AutomationFeasibility;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResponse {

    private Integer testcaseId;
    private String testcaseFormatId;
    private Integer featureId;
    private String title;
    private TestType testType;
    private TestPriority testPriority;
    private TestCaseStatus testcaseStatus;

    private Integer executionId;
    private String testExecution;
    private String testValidation;
    private String precondition;
    private String testData;
    private String executionSteps;
    private String uiValidations;
    private String dbValidations;
    private AutomationFeasibility automationFeasibility;
    private Integer bugsCount;
    private String comments;
}