package xyz.mobi.testingautomationtool.mapper.featureMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;

@Mapper(componentModel = "spring")
public interface FeatureMapper {
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Feature toEntity(FeatureRequest featureRequest);

    @Mapping(source = "project.projectId", target = "projectId")
    @Mapping(source = "createdBy.username",target = "createdBy")
    @Mapping(source = "createdAt",target = "createdAt")
    @Mapping(source = "updatedAt",target = "updatedAt")
    @Mapping(source = "featureId",target = "featureId")
    FeatureResponse toResponse(Feature feature);
}
