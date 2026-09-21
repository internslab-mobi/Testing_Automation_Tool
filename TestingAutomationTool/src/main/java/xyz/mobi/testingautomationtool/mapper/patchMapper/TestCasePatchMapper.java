package xyz.mobi.testingautomationtool.mapper.patchMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.ExecutionStatusResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;
import xyz.mobi.testingautomationtool.entity.TestingExecution;

@Mapper(componentModel = "spring")
public interface TestCasePatchMapper {

    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    TestCaseResponse toResponse(TestCase testCase);


    @Mapping(source = "testCase.testcaseId",target = "testCaseId")
    @Mapping(source = "executedBy.username",target = "executedBy")
    ExecutionStatusResponse patchExecutionUpdate(TestingExecution testingExecution);

}
