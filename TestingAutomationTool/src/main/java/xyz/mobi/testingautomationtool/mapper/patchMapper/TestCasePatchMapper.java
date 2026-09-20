package xyz.mobi.testingautomationtool.mapper.patchMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;

@Mapper(componentModel = "spring")
public interface TestCasePatchMapper {

    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "createdBy.username", target = "createdBy")
    TestCaseResponse toResponse(TestCase testCase);
}
