package xyz.mobi.testingautomationtool.mapper.postMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ExecutionRequest;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.TestCaseRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ExecutionResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ValidationResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {

    // TestCase Request → Entity
    @Mapping(target = "testcaseId", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "testcaseStatus", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
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
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "executedAt", ignore = true)
    TestingExecution toEntity(ExecutionRequest request);

    // Execution Entity → Response
    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = ".", target = "validations")
    ExecutionResponse toResponse(TestingExecution execution);

    ValidationResponse toValidationResponse(TestingExecution execution);
}
