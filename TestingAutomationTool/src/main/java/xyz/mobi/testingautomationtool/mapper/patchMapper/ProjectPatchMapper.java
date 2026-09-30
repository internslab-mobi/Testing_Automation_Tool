package xyz.mobi.testingautomationtool.mapper.patchMapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.entity.Project;

@Mapper(componentModel = "spring")
public interface ProjectPatchMapper {

    @Mapping(source = "updatedBy.username", target = "updatedBy")
//    @Mapping(source = "active", target = "isActive")
//    @Mapping(source = "deleted", target = "isDeleted")
    PatchProjectResponse toResponse(Project project);
}
