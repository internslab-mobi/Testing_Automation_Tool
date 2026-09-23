package xyz.mobi.testingautomationtool.mapper;

import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Component
public class TestCaseMapper {

    public TestCaseResponse toResponse(
            TestCase testCase,
            TestingExecution execution) {

        TestCaseResponse.TestCaseResponseBuilder builder = TestCaseResponse.builder()
                .testcaseId(testCase.getTestcaseId())
                .testcaseFormatId(testCase.getTestcaseFormatId())
                .featureId(testCase.getFeatureId())
                .title(testCase.getTitle())
                .testType(testCase.getTestType())
                .testPriority(testCase.getTestPriority())
                .testcaseStatus(testCase.getTestcaseStatus());

        if (execution != null) {
            builder.executionId(execution.getExecutionId())
                    .testExecution(execution.getTestExecution())
                    .testValidation(execution.getTestValidation())
                    .precondition(execution.getPrecondition())
                    .testData(execution.getTestData())
                    .executionSteps(execution.getExecutionSteps())
                    .uiValidations(execution.getUiValidations())
                    .dbValidations(execution.getDbValidations())
                    .automationFeasibility(execution.getAutomationFeasibility())
                    .bugsCount(execution.getBugsCount())
                    .comments(execution.getComments());
        }

        return builder.build();
    }
}
