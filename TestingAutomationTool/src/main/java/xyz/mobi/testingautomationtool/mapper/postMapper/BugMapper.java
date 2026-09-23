package xyz.mobi.testingautomationtool.mapper.postMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.BugPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.Bug;

@Mapper(componentModel = "spring")
public interface BugMapper {

    @Mapping(target = "bugId", ignore = true)
    @Mapping(target = "bugFormatId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "executedBy",ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "bugOccurrence", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "feature", ignore = true)
    Bug toEntity(BugRequest request);

    @Mapping(target = "bugId", ignore = true)
    @Mapping(target = "bugFormatId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "bugOccurrence", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Bug bug, BugPutRequest request);

    @Mapping(target = "testcaseId",
            expression = "java(bug.getTestCase() != null ? bug.getTestCase().getTestcaseId() : null)")
    @Mapping(target = "reportedBy",
            expression = "java(bug.getReportedBy() != null ? bug.getReportedBy().getUsername() : null)")
    @Mapping(target = "assignedTo",
            expression = "java(bug.getAssignedTo() != null ? bug.getAssignedTo().getUsername() : null)")
    BugResponse toResponse(Bug bug);
}