package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.enums.BugStatus;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
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

    @Mapping(target = "testcaseId", source = "testCase.testcaseId")
    @Mapping(target = "featureId", source = "feature.featureId")
    @Mapping(target = "reportedBy", source = "reportedBy.username")
    @Mapping(target = "assignedTo", source = "assignedTo.username")
    @Mapping(target = "executedBy", source = "executedBy.username")
    @Mapping(target = "updatedBy", source = "updatedBy.username")
    @Mapping(target = "active", source = "active")
    BugResponse toResponse(Bug bug);

    @Mapping(target = "bugId", ignore = true)
    @Mapping(target = "bugFormatId", ignore = true)
    @Mapping(target = "testCase", ignore = true)
    @Mapping(target = "feature", ignore = true)
    @Mapping(target = "reportedBy", ignore = true)
    @Mapping(target = "executedBy", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "resolvedAt", ignore = true)
    @Mapping(target = "bugOccurrence", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntity(@MappingTarget Bug bug, BugPutRequest request);

    default void patchEntity(@MappingTarget Bug bug, BugPatchRequest request) {
        if (request == null) return;
        if (request.getStatus() != null && request.getStatus().getStatus() != null) {
            bug.setStatus(request.getStatus().getStatus());
        }
    }

    @Mapping(target = "assignedTo", ignore = true)
    void updateAssignment(BugAssignRequest request, @MappingTarget Bug bug);

    void updateStatus(BugStatusRequest request, @MappingTarget Bug bug);

    default void updateDeveloperStatus(DeveloperBugStatusRequest request, @MappingTarget Bug bug) {
        if (request != null && request.getStatus() != null) {
            bug.setStatus(BugStatus.valueOf(request.getStatus().name()));
        }
    }
}
