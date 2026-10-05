package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse.*;
import xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse.FeatureInfo;
import xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse.TestCaseInfo;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.TestCase;
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

    @Mapping(target = "testCase", source = "testCase")
    @Mapping(target = "feature", source = "feature")
    @Mapping(target = "classification", source = ".")
    @Mapping(target = "users", source = ".")
    @Mapping(target = "resolution", source = ".")
    @Mapping(target = "audit", source = ".")
    BugResponse toResponse(Bug bug);


    @Mapping(target = "testcaseId", source = "testcaseId")
    @Mapping(target = "testcaseFormatId", source = "testcaseFormatId")
    TestCaseInfo toTestCaseInfo(TestCase testCase);


    @Mapping(target = "featureId", source = "featureId")
    @Mapping(target = "featureName", source = "featureName")
    FeatureInfo toFeatureInfo(Feature feature);


    @Mapping(target = "severity", source = "severity")
    @Mapping(target = "priority", source = "priority")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "status", source = "status")
    ClassificationInfo toClassificationInfo(Bug bug);


    @Mapping(target = "reportedBy", source = "reportedBy.username")
    @Mapping(target = "assignedTo", source = "assignedTo.username")
    @Mapping(target = "executedBy", source = "executedBy.username")
    @Mapping(target = "updatedBy", source = "updatedBy.username")
    UserInfo toUserInfo(Bug bug);


    @Mapping(target = "resolvedAt", source = "resolvedAt")
    @Mapping(target = "bugReoccurredId", ignore = true)
    @Mapping(target = "bugOccurrence", source = "bugOccurrence")
    ResolutionInfo toResolutionInfo(Bug bug);


    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    AuditInfo toAuditInfo(Bug bug);

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



    @Mapping(target = "assignedTo", ignore = true)
    void updateAssignment(BugAssignRequest request, @MappingTarget Bug bug);

    void updateStatus(BugStatusRequest request, @MappingTarget Bug bug);

    default void updateDeveloperStatus(DeveloperBugStatusRequest request, @MappingTarget Bug bug) {
        if (request != null && request.getStatus() != null) {
            bug.setStatus(BugStatus.valueOf(request.getStatus().name()));
        }
    }
}
