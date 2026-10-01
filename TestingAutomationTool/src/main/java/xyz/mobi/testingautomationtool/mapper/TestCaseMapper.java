package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.TestCaseDto.*;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDto.TestCaseExecutionRequest;
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

    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "deleted", target = "isDeleted")
    TestCaseResponse toResponse(TestCase testCase);

    default TestCaseResponse toResponse(TestCase testCase, TestingExecution execution) {
        if (testCase == null) {
            return null;
        }
        TestCaseResponse response = toResponse(testCase);
        if (execution != null) {
            response.setExecutionStatus(execution.getExecutionStatus());
            response.setAutomationFeasibility(execution.getAutomationFeasibility());
            response.setPrecondition(execution.getPrecondition());
            response.setTestData(execution.getTestData());
            response.setExecutionSteps(execution.getExecutionSteps());
            response.setUiValidations(execution.getUiValidations());
            response.setDbValidations(execution.getDbValidations());
            response.setComments(execution.getComments());
        }
        return response;
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

    @Mapping(target = "testcaseId", source = "testCase.testcaseId")
    @Mapping(target = "featureId", source = "testCase.feature.featureId")
    @Mapping(target = "title", source = "testCase.title")
    @Mapping(target = "testType", source = "testCase.testType")
    @Mapping(target = "testPriority", source = "testCase.testPriority")
    @Mapping(target = "username", source = "testCase.updatedBy.username")
    @Mapping(target = "updatedAt", source = "testCase.updatedAt")
    @Mapping(target = "validation", source = "execution")
    @Mapping(target = "dynamicFields", source = "testCase.dynamicFields")
    TestCasePutResponse toPutResponse(TestCase testCase, TestingExecution execution);

    @Mapping(target = "testExecution", source = "testExecution")
    @Mapping(target = "testValidation", source = "testValidation")
    @Mapping(target = "precondition", source = "precondition")
    @Mapping(target = "testData", source = "testData")
    @Mapping(target = "executionSteps", source = "executionSteps")
    @Mapping(target = "uiValidations", source = "uiValidations")
    @Mapping(target = "dbValidations", source = "dbValidations")
    @Mapping(target = "comments", source = "comments")
    TestCaseValidationResponse toValidationResponse(TestingExecution execution);

    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "updatedBy.username", target = "username")
    @Mapping(source = "active", target = "isActive")
    PatchTestCaseResponse toPatchResponse(TestCase testCase);

    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseFormatId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchEntity(@MappingTarget TestCase testCase, TestCasePatchRequest request);
}
