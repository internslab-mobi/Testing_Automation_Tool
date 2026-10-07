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

    default BugResponse toResponse(Bug bug) {
        if (bug == null) {
            return null;
        }

        BugResponse.BugDetails bugDetails = BugResponse.BugDetails.builder()
                .bugId(bug.getBugId())
                .bugFormatId(bug.getBugFormatId())
                .title(bug.getTitle())
                .description(bug.getDescription())
                .severity(bug.getSeverity())
                .priority(bug.getPriority())
                .category(bug.getCategory())
                .status(bug.getStatus())
                .bugOccurrence(bug.getBugOccurrence())
                .comments(bug.getComments())
                .isActive(bug.isActive())
                .isDeleted(bug.isDeleted())
                .build();

        BugResponse.TestCaseRef testCaseRef = null;
        if (bug.getTestCase() != null) {
            testCaseRef = BugResponse.TestCaseRef.builder()
                    .testcaseId(bug.getTestCase().getTestcaseId())
                    .testcaseFormatId(bug.getTestCase().getTestcaseFormatId())
                    .build();
        }

        BugResponse.FeatureRef featureRef = null;
        if (bug.getFeature() != null) {
            featureRef = BugResponse.FeatureRef.builder()
                    .featureId(bug.getFeature().getFeatureId())
                    .featureName(bug.getFeature().getFeatureName())
                    .build();
        }

        BugResponse.WorkflowInfo workflowInfo = BugResponse.WorkflowInfo.builder()
                .reportedBy(bug.getReportedBy() != null ? bug.getReportedBy().getUsername() : null)
                .assignedTo(bug.getAssignedTo() != null ? bug.getAssignedTo().getUsername() : null)
                .executedBy(bug.getExecutedBy() != null ? bug.getExecutedBy().getUsername() : null)
                .resolvedAt(bug.getResolvedAt())
                .build();

        BugResponse.AuditResponse audit = BugResponse.AuditResponse.builder()
                .updatedBy(bug.getUpdatedBy() != null ? bug.getUpdatedBy().getUsername() : null)
                .createdAt(bug.getCreatedAt())
                .updatedAt(bug.getUpdatedAt())
                .build();

        return BugResponse.builder()
                .bug(bugDetails)
                .testCase(testCaseRef)
                .feature(featureRef)
                .workflow(workflowInfo)
                .audit(audit)
                .dynamicFields(bug.getDynamicFields())
                .build();
    }

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
