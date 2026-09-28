package xyz.mobi.testingautomationtool.mapper.putMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectPutMapper {

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "version", ignore = true)
    Project putMethodMapper(ProjectPutRequest projectPutRequest, @MappingTarget Project project);

    @Mapping(source = "createdBy.username", target = "createdBy")
    @Mapping(source = "updatedBy.username", target = "updatedBy")
    @Mapping(source = "active", target = "isActive")
    @Mapping(source = "deleted", target = "isDeleted")
    ProjectPutResponse toResponse(Project project);
}
