package xyz.mobi.testingautomationtool.mapper;

import org.mapstruct.*;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.*;
import xyz.mobi.testingautomationtool.entity.Project;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProjectMapper {

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "status", ignore = true)
    Project toEntity(ProjectRequest request);

    default ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        ProjectResponse.ProjectDetails projectDetails = ProjectResponse.ProjectDetails.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .region(project.getRegion())
                .comments(project.getComments())
                .isActive(project.isActive())
                .isDeleted(project.isDeleted())
                .version(project.getVersion())
                .build();

        ProjectResponse.AuditResponse audit = ProjectResponse.AuditResponse.builder()
                .createdBy(project.getCreatedBy() != null ? project.getCreatedBy().getUserId() : null)
                .createdByName(project.getCreatedBy() != null ? project.getCreatedBy().getUsername() : null)
                .updatedByName(project.getUpdatedBy() != null ? project.getUpdatedBy().getUsername() : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();

        return ProjectResponse.builder()
                .project(projectDetails)
                .audit(audit)
                .build();
    }

    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "version", ignore = true)
    Project putMethodMapper(ProjectPutRequest projectPutRequest, @MappingTarget Project project);

    default ProjectPutResponse toPutResponse(Project project) {
        if (project == null) {
            return null;
        }

        ProjectPutResponse.ProjectDetails details = ProjectPutResponse.ProjectDetails.builder()
                .projectId(project.getProjectId())
                .projectName(project.getProjectName())
                .description(project.getDescription())
                .status(project.getStatus())
                .region(project.getRegion())
                .isActive(project.isActive())
                .isDeleted(project.isDeleted())
                .build();

        ProjectPutResponse.AuditResponse audit = ProjectPutResponse.AuditResponse.builder()
                .createdBy(project.getCreatedBy() != null ? project.getCreatedBy().getUsername() : null)
                .updatedBy(project.getUpdatedBy() != null ? project.getUpdatedBy().getUsername() : null)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();

        return ProjectPutResponse.builder()
                .project(details)
                .audit(audit)
                .build();
    }
}
