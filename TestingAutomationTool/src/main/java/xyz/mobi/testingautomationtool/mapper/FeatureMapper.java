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
    @Mapping(source = "updatedBy.userId", target = "updatedBy")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "featureVersion", target = "version")
    @Mapping(source = "featureVersion", target = "featureVersion")
    @Mapping(source = "version", target = "lockVersion")
    FeatureResponse toResponse(Feature feature);

    @Mapping(source = "project.projectId", target = "projectId")
    @Mapping(source = "createdBy.userId", target = "createdBy")
    @Mapping(source = "featureVersion", target = "version")
    @Mapping(source = "featureVersion", target = "featureVersion")
    FeaturePutResponse toPutResponse(Feature feature);

    @Mapping(target = "featureId", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "duration", ignore = true)
    @Mapping(target = "startTime", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    Feature updateEntityFromPut(FeaturePutRequest request);

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
