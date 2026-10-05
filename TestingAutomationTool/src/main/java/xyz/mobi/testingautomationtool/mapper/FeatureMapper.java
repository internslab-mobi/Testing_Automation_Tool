package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.FeatureDto.*;
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

    @Mapping(source = "project.projectId", target = "projectId")
    @Mapping(source = "project.projectName", target = "projectName")
    @Mapping(source = "createdBy.userId", target = "createdBy")
    @Mapping(source = "createdBy.username", target = "creatorName")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "featureVersion", target = "featureVersion")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    FeatureResponse toResponse(Feature feature);

    @Mapping(source = "project.projectId", target = "projectId")
    @Mapping(source = "updatedBy.userId", target = "updatedBy")
    @Mapping(source = "featureVersion", target = "featureVersion")
    FeaturePutResponse toPutResponse(Feature feature);

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
