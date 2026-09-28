package xyz.mobi.testingautomationtool.mapper.getMapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;

@Mapper(componentModel = "spring")
public interface FeatureMapper {

    @Mapping(
            target = "projectId",
            source = "project.projectId"
    )
    @Mapping(
            target = "createdBy",
            source = "createdBy.userId"
    )
    FeatureResponse toResponse(Feature feature);
}
