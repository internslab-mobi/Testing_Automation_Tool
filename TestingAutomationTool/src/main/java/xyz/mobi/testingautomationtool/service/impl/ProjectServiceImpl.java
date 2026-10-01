package xyz.mobi.testingautomationtool.service.impl;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ProjectDto.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.ProjectMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;
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
    private final ProjectMapper projectMapper;
    private final AuthService authService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByProjectName(request.getProjectName())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        User user = authService.getCurrentUser();
        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(user);
        project.setUpdatedBy(user);
        project.setActive(true);
        project.setDeleted(false);
        if (project.getStatus() == null) {
            project.setStatus(ProjectStatus.ACTIVE);
        }

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

    @Override
    public xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse uploadAttachment(MultipartFile file, Integer projectId) throws IOException {
        Feature feature = featureRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project is not present for this id: " + projectId));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "Project_attachment_" + System.currentTimeMillis();
        }

        String fileType = file.getContentType();
        Long fileSize = file.getSize();
        byte[] bytes = file.getBytes();

        User user = authService.getCurrentUser();

        Attachment attachment = Attachment.builder()
                .attachmentType(AttachmentType.PROJECT)
                .feature(feature)
                .project(feature.getProject())
                .fileName(filename)
                .fileType(fileType)
                .fileSize(fileSize)
                .fileBlob(bytes)
                .uploadedBy(user)
                .updatedBy(user)
                .isActive(true)
                .isDeleted(false)
                .build();

        attachmentRepository.save(attachment);

        return xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .featureId(projectId)
                .fileName(filename)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadedBy(user != null ? user.getUserId() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    @Override
    public ProjectPutResponse updateProject(Integer id, ProjectPutRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (project.isDeleted() || !project.isActive()) {
            throw new IllegalStateException("Cannot update disabled/deleted project with ID: " + id);
        }

        projectMapper.putMethodMapper(request, project);
        project.setUpdatedBy(authService.getCurrentUser());

        Project savedProject = projectRepository.save(project);
        return projectMapper.toPutResponse(savedProject);
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

        User currentUser = authService.getCurrentUser();
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

        if (request.getStatus() != null || request.getIsActive() != null) {
            String roleName = (currentUser != null && currentUser.getRole() != null && currentUser.getRole().getRole() != null)
                    ? currentUser.getRole().getRole().toUpperCase() : "";
            boolean isManager = roleName.contains("MANAGER") || roleName.contains("ADMIN");
            if (!isManager) {
                throw new org.springframework.security.access.AccessDeniedException("Only MANAGER role can change project status or active state");
            }

            if (request.getStatus() != null) {
                ProjectStatus status = request.getStatus();
                project.setStatus(status);
                if (status == ProjectStatus.ACTIVE) {
                    project.setActive(true);
                    bugRepository.activateBugsByProjectId(id);
                    attachmentRepository.activateAttachmentsByProjectId(id);
                    testCaseRepository.activateTestCasesByProjectId(id);
                    featureRepository.activateFeaturesByProjectId(id);
                } else if (status == ProjectStatus.INACTIVE) {
                    project.setActive(false);
                    bugRepository.deactivateBugsByProjectId(id);
                    attachmentRepository.deactivateAttachmentsByProjectId(id);
                    testCaseRepository.deactivateTestCasesByProjectId(id);
                    featureRepository.deactivateFeaturesByProjectId(id);
                }
                updatedFields.add("status");
            }

            if (request.getIsActive() != null) {
                boolean active = request.getIsActive();
                project.setActive(active);
                project.setStatus(active ? ProjectStatus.ACTIVE : ProjectStatus.INACTIVE);
                if (active) {
                    bugRepository.activateBugsByProjectId(id);
                    attachmentRepository.activateAttachmentsByProjectId(id);
                    testCaseRepository.activateTestCasesByProjectId(id);
                    featureRepository.activateFeaturesByProjectId(id);
                } else {
                    bugRepository.deactivateBugsByProjectId(id);
                    attachmentRepository.deactivateAttachmentsByProjectId(id);
                    testCaseRepository.deactivateTestCasesByProjectId(id);
                    featureRepository.deactivateFeaturesByProjectId(id);
                }
                updatedFields.add("isActive");
            }
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        project.setUpdatedBy(currentUser);
        projectRepository.save(project);

        return "Project with ID " + id + " updated successfully. Changed fields: " + String.join(", ", updatedFields);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findByIsDeletedFalse().stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProjectById(Integer projectId) {
        Project project = projectRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        return projectMapper.toResponse(project);
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

                try {
                    Integer id = Integer.valueOf(trimmed);
                    orPredicates.add(cb.equal(root.get("projectId"), id));
                } catch (NumberFormatException ignored) {
                }

                predicates.add(cb.or(orPredicates.toArray(new Predicate[0])));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return projectRepository.findAll(spec).stream()
                .map(projectMapper::toResponse)
                .toList();
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
    public PatchProjectDeleteResponse softDeleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        project.setDeleted(true);
        project.setActive(false);
        project.setStatus(ProjectStatus.INACTIVE);
        project.setUpdatedBy(authService.getCurrentUser());
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
