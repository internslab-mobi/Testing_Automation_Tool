package xyz.mobi.testingautomationtool.mapper;

import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.*;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Component
public class TestCaseMapper {

    public TestCaseResponse toResponse(
            TestCase testCase,
            TestingExecution execution) {

        Integer featureId =
                testCase.getFeature() != null
                        ? testCase.getFeature().getFeatureId()
                        : null;

        TestCaseDetails testCaseDetails = TestCaseDetails.builder()
                .testcaseId(testCase.getTestcaseId())
                .testcaseFormatId(testCase.getTestcaseFormatId())
                .featureId(featureId)
                .title(testCase.getTitle())
                .testType(testCase.getTestType())
                .testPriority(testCase.getTestPriority())
                .testcaseStatus(testCase.getTestcaseStatus())
                .build();

        AuditResponse audit = AuditResponse.builder()
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .build();

        TestCaseResponse.TestCaseResponseBuilder response =
                TestCaseResponse.builder()
                        .testCase(testCaseDetails)
                        .audit(audit);

        if (execution != null) {

            ExecutionResponse executionResponse = ExecutionResponse.builder()
                    .executionId(execution.getExecutionId())
                    .testExecution(execution.getTestExecution())
                    .precondition(execution.getPrecondition())
                    .testData(execution.getTestData())
                    .executionSteps(execution.getExecutionSteps())
                    .build();

            ValidationResponse validationResponse = ValidationResponse.builder()
                    .testValidation(execution.getTestValidation())
                    .uiValidations(execution.getUiValidations())
                    .dbValidations(execution.getDbValidations())
                    .build();

            AutomationResponse automationResponse = AutomationResponse.builder()
                    .automationFeasibility(execution.getAutomationFeasibility())
                    .build();

            BugSummaryResponse bugSummaryResponse = BugSummaryResponse.builder()
                    .bugsCount(execution.getBugsCount())
                    .build();

            response
                    .execution(executionResponse)
                    .validations(validationResponse)
                    .automation(automationResponse)
                    .bugSummary(bugSummaryResponse)
                    .comments(execution.getComments());
        }

        return response.build();
    }
}