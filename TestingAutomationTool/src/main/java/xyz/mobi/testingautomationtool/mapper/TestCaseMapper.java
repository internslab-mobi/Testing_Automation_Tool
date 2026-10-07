package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.*;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestCaseMapper {

    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseStatus", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestCase toEntity(TestCaseRequest request);

    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestCase toEntity(TestCaseExecutionRequest request);

    default TestCaseResponse toResponse(TestCase testCase) {
        return toResponse(testCase, null);
    }

    default TestCaseResponse toResponse(TestCase testCase, TestingExecution execution) {
        if (testCase == null) {
            return null;
        }

        TestCaseResponse.TestCaseDetails testCaseDetails = TestCaseResponse.TestCaseDetails.builder()
                .testcaseId(testCase.getTestcaseId())
                .testcaseFormatId(testCase.getTestcaseFormatId())
                .featureId(testCase.getFeature() != null ? testCase.getFeature().getFeatureId() : null)
                .title(testCase.getTitle())
                .testType(testCase.getTestType())
                .testPriority(testCase.getTestPriority())
                .testcaseStatus(testCase.getTestcaseStatus())
                .isActive(testCase.isActive())
                .isDeleted(testCase.isDeleted())
                .version(testCase.getVersion())
                .build();

        TestCaseResponse.AuditResponse audit = TestCaseResponse.AuditResponse.builder()
                .createdBy(testCase.getCreatedBy() != null ? testCase.getCreatedBy().getUsername() : null)
                .updatedBy(testCase.getUpdatedBy() != null ? testCase.getUpdatedBy().getUsername() : null)
                .createdAt(testCase.getCreatedAt())
                .updatedAt(testCase.getUpdatedAt())
                .build();

        TestCaseResponse.ExecutionResponse executionResponse = null;
        TestCaseResponse.ValidationResponse validationResponse = null;
        TestCaseResponse.AutomationResponse automationResponse = null;
        TestCaseResponse.BugSummaryResponse bugSummaryResponse = null;

        if (execution != null) {
            executionResponse = TestCaseResponse.ExecutionResponse.builder()
                    .executionId(execution.getExecutionId())
                    .executionStatus(execution.getExecutionStatus())
                    .testExecution(execution.getTestExecution())
                    .precondition(execution.getPrecondition())
                    .testData(execution.getTestData())
                    .executionSteps(execution.getExecutionSteps())
                    .comments(execution.getComments())
                    .build();

            validationResponse = TestCaseResponse.ValidationResponse.builder()
                    .testValidation(execution.getTestValidation())
                    .uiValidations(execution.getUiValidations())
                    .dbValidations(execution.getDbValidations())
                    .build();

            automationResponse = TestCaseResponse.AutomationResponse.builder()
                    .automationFeasibility(execution.getAutomationFeasibility())
                    .build();

            bugSummaryResponse = TestCaseResponse.BugSummaryResponse.builder()
                    .bugsCount(execution.getBugsCount())
                    .build();
        }

        return TestCaseResponse.builder()
                .testCase(testCaseDetails)
                .execution(executionResponse)
                .validations(validationResponse)
                .automation(automationResponse)
                .bugSummary(bugSummaryResponse)
                .audit(audit)
                .dynamicFields(testCase.getDynamicFields())
                .build();
    }

    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseFormatId", ignore = true)
    @Mapping(target = "testcaseStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestCase putMethodMapper(TestCasePutRequest testCasePutRequest, @MappingTarget TestCase testCase);

    default TestCasePutResponse toPutResponse(TestCase testCase, TestingExecution execution) {
        if (testCase == null) {
            return null;
        }

        TestCasePutResponse.TestCaseDetails details = TestCasePutResponse.TestCaseDetails.builder()
                .testcaseId(testCase.getTestcaseId())
                .featureId(testCase.getFeature() != null ? testCase.getFeature().getFeatureId() : null)
                .title(testCase.getTitle())
                .testType(testCase.getTestType())
                .testPriority(testCase.getTestPriority())
                .build();

        TestCasePutResponse.AuditResponse audit = TestCasePutResponse.AuditResponse.builder()
                .updatedBy(testCase.getUpdatedBy() != null ? testCase.getUpdatedBy().getUsername() : null)
                .updatedAt(testCase.getUpdatedAt())
                .build();

        TestCasePutResponse.ValidationResponse val = null;
        if (execution != null) {
            val = TestCasePutResponse.ValidationResponse.builder()
                    .testExecution(execution.getTestExecution())
                    .testValidation(execution.getTestValidation())
                    .precondition(execution.getPrecondition())
                    .testData(execution.getTestData())
                    .executionSteps(execution.getExecutionSteps())
                    .uiValidations(execution.getUiValidations())
                    .dbValidations(execution.getDbValidations())
                    .comments(execution.getComments())
                    .build();
        }

        return TestCasePutResponse.builder()
                .testCase(details)
                .validation(val)
                .audit(audit)
                .dynamicFields(testCase.getDynamicFields())
                .build();
    }

    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseFormatId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchEntity(@MappingTarget TestCase testCase, TestCasePatchRequest request);
}
