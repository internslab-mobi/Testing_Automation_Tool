package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.PackageMethodDto.TestPatchMethodDto;
import xyz.mobi.testingautomationtool.dto.TestcaseDTO.TestCaseResponse;
import xyz.mobi.testingautomationtool.entity.TestCase;

@Mapper(componentModel = "spring")
public interface PatchMethodMapper {
    TestCaseResponse testPatchToResponse(TestCase testCase);
}
