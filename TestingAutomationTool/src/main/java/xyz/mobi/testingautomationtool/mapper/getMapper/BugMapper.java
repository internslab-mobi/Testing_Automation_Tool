package xyz.mobi.testingautomationtool.mapper.getMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse;
import xyz.mobi.testingautomationtool.entity.Bug;

@Mapper(componentModel = "spring")
public interface BugMapper {

    @Mapping(source = "bugId", target = "bugId")
    @Mapping(source = "bugFormatId", target = "bugFormatId")

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = "feature.featureId", target = "featureId")

    @Mapping(source = "title", target = "title")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "severity", target = "severity")
    @Mapping(source = "priority", target = "priority")
    @Mapping(source = "status", target = "status")

    @Mapping(source = "reportedBy.userId", target = "reportedBy")
    @Mapping(source = "assignedTo.userId", target = "assignedTo")

    @Mapping(source = "resolvedAt", target = "resolvedAt")

    @Mapping(source = "bugReoccurred.bugId", target = "bugReoccurredId")
    @Mapping(source = "bugOccurrence", target = "bugOccurrence")

    @Mapping(source = "active", target = "active")
    BugResponse toResponse(Bug bug);
}