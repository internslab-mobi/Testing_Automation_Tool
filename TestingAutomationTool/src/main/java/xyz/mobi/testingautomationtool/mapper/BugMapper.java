package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.BugDTO.*;
import xyz.mobi.testingautomationtool.entity.Bug;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BugMapper {

    @Mapping(target = "bugId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "bugOccurrence", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    Bug toEntity(BugRequest request);

    @Mapping(source = "testCase.testcaseId", target = "testcaseId")
    @Mapping(source = "testCase.testcaseFormatId", target = "testcaseFormatId")
    @Mapping(source = "feature.featureId", target = "featureId")
    @Mapping(source = "feature.featureName", target = "featureName")
    @Mapping(source = "reportedBy.username", target = "reportedBy")
    @Mapping(source = "assignedTo.username", target = "assignedTo")
    @Mapping(source = "executedBy.username", target = "executedBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "deleted", target = "isDeleted")
    BugResponse toResponse(Bug bug);

    @Mapping(target = "bugId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(
            @MappingTarget Bug bug,
            BugPutRequest request
    );

}

