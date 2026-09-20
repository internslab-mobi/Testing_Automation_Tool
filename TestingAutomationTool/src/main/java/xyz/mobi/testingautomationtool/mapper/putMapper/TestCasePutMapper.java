package xyz.mobi.testingautomationtool.mapper.putMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.TestCasePutResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCasePutMapper {

    TestCase putMethodMapper(TestCasePutRequest testCasePutRequest,
                             @MappingTarget TestCase testCase);

    TestingExecution putMethodToExecution(TestCasePutRequest testCasePutRequest,
                                          @MappingTarget TestingExecution testingExecution);

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = "testCase.feature.featureId", target = "featureId")
    @Mapping(source = "testCase.title", target = "title")
    @Mapping(source = "testCase.testType", target = "testType")
    @Mapping(source = "testCase.testPriority", target = "testPriority")
    @Mapping(source = "testCase.testcaseStatus", target = "testcaseStatus")
    @Mapping(source = "testCase.createdBy.username", target = "username")
    @Mapping(source = "testCase.updatedAt", target = "updatedAt")
    @Mapping(source = "testCase.testcaseFormatId", target = "testcaseFormatId")
    @Mapping(source = "testingExecution.testExecution", target = "testExecution")
    @Mapping(source = "testingExecution.testValidation", target = "testValidation")
    @Mapping(source = "testingExecution.precondition", target = "precondition")
    @Mapping(source = "testingExecution.testData", target = "testData")
    @Mapping(source = "testingExecution.executionSteps", target = "executionSteps")
    @Mapping(source = "testingExecution.uiValidations", target = "uiValidations")
    @Mapping(source = "testingExecution.dbValidations", target = "dbValidations")
    @Mapping(source = "testingExecution.comments", target = "comments")
    TestCasePutResponse convertToResponse(
            TestCase testCase,
            TestingExecution testingExecution
    );
}
