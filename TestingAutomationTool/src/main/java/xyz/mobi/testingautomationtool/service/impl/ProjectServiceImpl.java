package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findByIsDeletedFalse().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProjectById(Integer projectId) {
        Project project = projectRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        return toResponse(project);
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .projectId(p.getProjectId())
                .projectName(p.getProjectName())
                .description(p.getDescription())
                .status(p.getStatus())
                .region(p.getRegion())
                .isActive(p.isActive())
                .createdBy(p.getCreatedBy() != null ? p.getCreatedBy().getUserId() : null)
                .creatorName(p.getCreatedBy() != null ? (p.getCreatedBy().getFullName() != null ? p.getCreatedBy().getFullName() : p.getCreatedBy().getUsername()) : null)
                .updatedBy(p.getUpdatedBy() != null ? p.getUpdatedBy().getUserId() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
