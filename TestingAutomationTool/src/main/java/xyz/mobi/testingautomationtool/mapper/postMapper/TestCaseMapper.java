package xyz.mobi.testingautomationtool.mapper.postMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ExecutionRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {

    // TestCase Request → Entity
    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseStatus", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    TestCase toEntity(TestCaseRequest request);

    // TestCase Entity → Response
    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    TestCaseResponse toResponse(TestCase testCase);

    // Execution Request → Entity
    @Mapping(target = "executionId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "bugsCount", ignore = true)
    @Mapping(target = "executionNumber", ignore = true)
    TestingExecution toEntity(ExecutionRequest request);

    // Execution Entity → Response
    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    ExecutionResponse toResponse(TestingExecution execution);
}
