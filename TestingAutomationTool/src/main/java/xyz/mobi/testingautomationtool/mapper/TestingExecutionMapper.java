package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.TestCaseDTO.TestCasePutRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionRequest;
import xyz.mobi.testingautomationtool.dto.TestingExecutionDTO.TestingExecutionResponse;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TestingExecutionMapper {

    @Mapping(target = "executionId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "bugsCount", ignore = true)
    @Mapping(target = "executionNumber", ignore = true)
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "executedAt", ignore = true)
    @Mapping(target = "executionStatus", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestingExecution toEntity(TestingExecutionRequest request);

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = "executedBy.userId", target = "executedBy")
    TestingExecutionResponse toResponse(TestingExecution testingExecution);

    @Mapping(target = "executionId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "bugsCount", ignore = true)
    @Mapping(target = "executionNumber", ignore = true)
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "executedAt", ignore = true)
    @Mapping(target = "executionStatus", ignore = true)
    @Mapping(target = "version", ignore = true)
    TestingExecution putMethodToExecution(TestCasePutRequest testCasePutRequest, @MappingTarget TestingExecution testingExecution);
}
