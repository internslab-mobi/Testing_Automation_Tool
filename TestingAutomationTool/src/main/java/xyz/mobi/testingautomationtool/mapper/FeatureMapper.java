package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.*;
import xyz.mobi.testingautomationtool.entity.Feature;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FeatureMapper {

    @Mapping(target = "featureId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "featureVersion", source = "featureVersion")
    Feature toEntity(FeatureRequest featureRequest);

    default FeatureResponse toResponse(Feature feature) {
        if (feature == null) {
            return null;
        }

        FeatureResponse.FeatureDetails featureDetails = FeatureResponse.FeatureDetails.builder()
                .featureId(feature.getFeatureId())
                .featureName(feature.getFeatureName())
                .description(feature.getDescription())
                .status(feature.getStatus())
                .duration(feature.getDuration())
                .sprint(feature.getSprint())
                .featureVersion(feature.getFeatureVersion())
                .startTime(feature.getStartTime())
                .comments(feature.getComments())
                .isActive(feature.isActive())
                .build();

        FeatureResponse.ProjectRef projectRef = null;
        if (feature.getProject() != null) {
            projectRef = FeatureResponse.ProjectRef.builder()
                    .projectId(feature.getProject().getProjectId())
                    .projectName(feature.getProject().getProjectName())
                    .build();
        }

        FeatureResponse.AuditResponse audit = FeatureResponse.AuditResponse.builder()
                .createdBy(feature.getCreatedBy() != null ? feature.getCreatedBy().getUserId() : null)
                .creatorName(feature.getCreatedBy() != null ? feature.getCreatedBy().getUsername() : null)
                .createdAt(feature.getCreatedAt())
                .build();

        return FeatureResponse.builder()
                .feature(featureDetails)
                .project(projectRef)
                .audit(audit)
                .build();
    }

    default FeaturePutResponse toPutResponse(Feature feature) {
        if (feature == null) {
            return null;
        }

        FeaturePutResponse.FeatureDetails details = FeaturePutResponse.FeatureDetails.builder()
                .featureId(feature.getFeatureId())
                .featureName(feature.getFeatureName())
                .description(feature.getDescription())
                .status(feature.getStatus())
                .sprint(feature.getSprint())
                .featureVersion(feature.getFeatureVersion())
                .duration(feature.getDuration())
                .startTime(feature.getStartTime())
                .build();

        FeaturePutResponse.ProjectRef projectRef = null;
        if (feature.getProject() != null) {
            projectRef = FeaturePutResponse.ProjectRef.builder()
                    .projectId(feature.getProject().getProjectId())
                    .build();
        }

        FeaturePutResponse.AuditResponse audit = FeaturePutResponse.AuditResponse.builder()
                .updatedBy(feature.getUpdatedBy() != null ? feature.getUpdatedBy().getUserId() : null)
                .build();

        return FeaturePutResponse.builder()
                .feature(details)
                .project(projectRef)
                .audit(audit)
                .build();
    }

    @Mapping(target = "featureId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromPut(@MappingTarget Feature feature, FeaturePutRequest request);

    @Mapping(target = "featureId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    void patchEntity(@MappingTarget Feature feature, FeaturePatchRequest request);
}
