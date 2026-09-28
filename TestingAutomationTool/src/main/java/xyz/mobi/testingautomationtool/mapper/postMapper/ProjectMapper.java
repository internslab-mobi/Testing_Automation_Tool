package xyz.mobi.testingautomationtool.mapper.postMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ProjectRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Project toEntity(ProjectRequest request);

    @Mapping(
            target = "createdBy",
            source = "createdBy.userId"
    )
    ProjectResponse toResponse(Project project);
}