package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.putMapper.ProjectPutMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.service.ProjectService;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;

import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final FeatureRepository featureRepository;
    private final TestCaseRepository testCaseRepository;
    private final BugRepository bugRepository;
    private final TestingExecutionRepository testingExecutionRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final ProjectPutMapper projectPutMapper;
    private final AuthService authService;

    @Override
    public ProjectPutResponse updateProject(Integer id, ProjectPutRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (project.isDeleted() || !project.isActive()) {
            throw new IllegalStateException("Cannot update disabled/deleted project with ID: " + id);
        }

        projectPutMapper.putMethodMapper(request, project);
        User dummyUser = userRepository.findById(2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: 2"));
        project.setUpdatedBy(dummyUser);

        Project savedProject = projectRepository.save(project);
        return projectPutMapper.toResponse(savedProject);
    }

    @Override
    public String patchProject(Integer id, ProjectPatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Patch request cannot be null");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (project.isDeleted()) {
            throw new IllegalStateException("Project is permanently deleted and cannot be modified with ID: " + id);
        }

        List<String> updatedFields = new ArrayList<>();

        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
            updatedFields.add("projectName");
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
            updatedFields.add("description");
        }

        if (request.getRegion() != null) {
            project.setRegion(request.getRegion());
            updatedFields.add("region");
        }

        if (request.getComments() != null) {
            project.setComments(request.getComments());
            updatedFields.add("comments");
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        User dummyUser = userRepository.findById(2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: 2"));
        project.setUpdatedBy(dummyUser);

        projectRepository.save(project);

        return "Project with ID " + id + " updated successfully. Changed fields: " + String.join(", ", updatedFields);
    }

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

    @Override
    public String hardDeleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        String projectName = project.getProjectName();
        commentRepository.deleteByProjectId(id);
        attachmentRepository.deleteByProjectId(id);
        bugRepository.deleteByProjectId(id);
        testingExecutionRepository.deleteByProjectId(id);
        testCaseRepository.deleteByProjectId(id);
        featureRepository.deleteByProjectId(id);
        projectRepository.delete(project);

        return "Project '" + projectName + "' and all associated features, test cases, executions, and bugs deleted successfully";
    }
    @Override
    public PatchProjectResponse getProjectStatus(ProjectStatus status,
                                                 Integer projectId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        User user = authService.getCurrentUser();
        if(status == ProjectStatus.ACTIVE){
            project.setActive(true);
            project.setStatus(ProjectStatus.ACTIVE);
            projectRepository.save(project);
            bugRepository.activateBugsByProjectId(projectId);
            attachmentRepository.activateAttachmentsByProjectId(projectId);
            testCaseRepository.activateTestCasesByProjectId(projectId);
            featureRepository.activateFeaturesByProjectId(projectId);


        } else if (status == ProjectStatus.INACTIVE) {
            project.setActive(false);
            project.setStatus(ProjectStatus.INACTIVE);
            projectRepository.save(project);
            bugRepository.deactivateBugsByProjectId(projectId);
            attachmentRepository.deactivateAttachmentsByProjectId(projectId);
            testCaseRepository.deactivateTestCasesByProjectId(projectId);
            featureRepository.deactivateFeaturesByProjectId(projectId);
        }


        return PatchProjectResponse.builder()
                .projectId(project.getProjectId())
                .updatedAt(project.getUpdatedAt())
                .status(project.getStatus())
                .updatedBy(user.getFullName())
                .build();
    }



    @Override
    public PatchProjectDeleteResponse softDeleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        project.setDeleted(true);
        project.setActive(false);
        project.setStatus(ProjectStatus.INACTIVE);
        projectRepository.save(project);
        bugRepository.deactivateBugsByProjectId(id);
        attachmentRepository.deactivateAttachmentsByProjectId(id);
        testCaseRepository.deactivateTestCasesByProjectId(id);
        featureRepository.deactivateFeaturesByProjectId(id);

        return PatchProjectDeleteResponse.builder()
                .projectId(id)
                .message("Project and all associated features, test cases, and bugs deactivated successfully")
                .build();
    }

}
