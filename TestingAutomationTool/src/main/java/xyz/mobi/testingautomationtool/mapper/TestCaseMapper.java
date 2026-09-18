package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.TestCaseExecutionDTO.TestCaseExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {


    TestCaseResponse toResponse(TestCase testCase);

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    TestingExecutionResponse toResponse(TestingExecution testingExecution);
}
