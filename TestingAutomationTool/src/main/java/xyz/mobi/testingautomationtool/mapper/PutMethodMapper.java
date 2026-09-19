package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodDto;
import xyz.mobi.testingautomationtool.dto.PutMethodDtos.PutMethodResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface PutMethodMapper {
    TestCase putMethodMapper(PutMethodDto putMethodDto, @MappingTarget TestCase testCase);
    TestingExecution putMethodToExecution(PutMethodDto dto,@MappingTarget TestingExecution testingExecution);

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = "testCase.feature", target = "feature")
    @Mapping(source = "testCase.title", target = "title")
    @Mapping(source = "testCase.testType", target = "testType")
    @Mapping(source = "testCase.testPriority", target = "testPriority")
    @Mapping(source = "testCase.testcaseStatus", target = "testcaseStatus")
    @Mapping(source = "testCase.createdBy", target = "createdBy")
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
    @Mapping(source = "testCase.active",target = "isActive")
    PutMethodResponse convertToResponse(
            TestCase testCase,
            TestingExecution testingExecution
    );
}
