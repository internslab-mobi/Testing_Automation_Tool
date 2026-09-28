package xyz.mobi.testingautomationtool.mapper.putMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCaseValidationResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCasePutMapper {

    TestCase putMethodMapper(TestCasePutRequest testCasePutRequest,
                             @MappingTarget TestCase testCase);

    TestingExecution putMethodToExecution(TestCasePutRequest testCasePutRequest,
                                          @MappingTarget TestingExecution testingExecution);

    @Mapping(target = "testcaseId", source = "testCase.testcaseId")
    @Mapping(target = "featureId", source = "testCase.feature.featureId")
    @Mapping(target = "title", source = "testCase.title")
    @Mapping(target = "testType", source = "testCase.testType")
    @Mapping(target = "testPriority", source = "testCase.testPriority")
    @Mapping(target = "username", source = "testCase.updatedBy.username")
    @Mapping(target = "updatedAt", source = "testCase.updatedAt")
    @Mapping(target = "validation", source = "execution")
    @Mapping(target = "dynamicFields", source = "testCase.dynamicFields")
    TestCasePutResponse toResponse(
            TestCase testCase,
            TestingExecution execution
    );
    @Mapping(target = "testExecution", source = "testExecution")
    @Mapping(target = "testValidation", source = "testValidation")
    @Mapping(target = "precondition", source = "precondition")
    @Mapping(target = "testData", source = "testData")
    @Mapping(target = "executionSteps", source = "executionSteps")
    @Mapping(target = "uiValidations", source = "uiValidations")
    @Mapping(target = "dbValidations", source = "dbValidations")
    @Mapping(target = "comments", source = "comments")
    TestCaseValidationResponse toValidationResponse(
            TestingExecution execution
    );
}
