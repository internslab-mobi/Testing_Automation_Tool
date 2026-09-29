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

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.ArrayList;
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

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjects(String keyword, ProjectStatus status) {
        Specification<Project> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isFalse(root.get("isDeleted")));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String trimmed = keyword.trim();
                String searchLower = trimmed.toLowerCase();
                String pattern = "%" + searchLower + "%";

                List<Predicate> orPredicates = new ArrayList<>();
                orPredicates.add(cb.like(cb.lower(root.get("projectName")), pattern));
                orPredicates.add(cb.like(cb.lower(root.get("description")), pattern));
                orPredicates.add(cb.like(cb.lower(root.get("region")), pattern));

                // Enum search for ProjectStatus
                List<ProjectStatus> matchingStatuses = new ArrayList<>();
                for (ProjectStatus ps : ProjectStatus.values()) {
                    String statusName = ps.name().toLowerCase();
                    String statusWithSpace = statusName.replace('_', ' ');
                    if (statusName.contains(searchLower) || statusWithSpace.contains(searchLower)) {
                        matchingStatuses.add(ps);
                    }
                }
                if (!matchingStatuses.isEmpty()) {
                    orPredicates.add(root.get("status").in(matchingStatuses));
                }

                // Numeric search for projectId
                try {
                    Integer id = Integer.valueOf(trimmed);
                    orPredicates.add(cb.equal(root.get("projectId"), id));
                } catch (NumberFormatException ignored) {
                    // Keyword is not an integer; skip numeric match
                }

                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return projectRepository.findAll(spec).stream()
                .map(this::toResponse)
                .toList();
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
