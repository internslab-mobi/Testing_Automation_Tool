package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.patchmethodDTO.ProjectPatchRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.ProjectPutRequest;
import xyz.mobi.testingautomationtool.dto.response.DeleteMethodDto.PatchProjectDeleteResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.PatchProjectResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.ProjectPutResponse;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.putMapper.ProjectPutMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.ProjectService;

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
    public PatchProjectResponse patchProject(Integer id, ProjectPatchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Patch request cannot be null");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        if (project.isDeleted()) {
            throw new IllegalStateException("Project is permanently deleted and cannot be modified with ID: " + id);
        }

        boolean hasUpdate = request.getProjectName() != null
                || request.getDescription() != null
                || request.getRegion() != null
                || request.getStatus() != null
                || request.getIsActive() != null
                || request.getIsDeleted() != null
                || request.getComments() != null;

        if (!hasUpdate) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        if (request.getProjectName() != null) {
            project.setProjectName(request.getProjectName());
        }

        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }

        if (request.getRegion() != null) {
            project.setRegion(request.getRegion());
        }

        if (request.getStatus() != null) {
            project.setStatus(request.getStatus());
        }

        if (request.getComments() != null) {
            project.setComments(request.getComments());
        }

        if (request.getIsActive() != null) {
            project.setActive(request.getIsActive());
            if (request.getIsActive()) {
                project.setStatus(ProjectStatus.ACTIVE);
            } else {
                project.setStatus(ProjectStatus.INACTIVE);
            }
        }

        if (request.getIsDeleted() != null && request.getIsDeleted()) {
            project.setDeleted(true);
            project.setActive(false);
            project.setStatus(ProjectStatus.INACTIVE);
        }

        User dummyUser = userRepository.findById(2)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: 2"));
        project.setUpdatedBy(dummyUser);

        Project savedProject = projectRepository.save(project);

        PatchProjectResponse response = PatchProjectResponse.builder()
                .projectId(savedProject.getProjectId())
                .updatedAt(savedProject.getUpdatedAt())
                .updatedBy(dummyUser.getFullName())
                .build();

        if (request.getProjectName() != null) {
            response.setProjectName(savedProject.getProjectName());
        }

        if (request.getComments() != null) {
            response.setComments(savedProject.getComments());
        }

        if (request.getDescription() != null) {
            response.setDescription(savedProject.getDescription());
        }

        if (request.getRegion() != null) {
            response.setRegion(savedProject.getRegion());
        }

        if (request.getStatus() != null) {
            response.setStatus(savedProject.getStatus());
        }

        if (request.getIsActive() != null) {
            response.setIsActive(savedProject.isActive());
        }

        if (request.getIsDeleted() != null) {
            response.setIsDeleted(savedProject.isDeleted());
        }

        return response;
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

    @Override
    public String hardDeleteProject(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

        String projectName = project.getProjectName();

        // Delete in reverse dependency order to respect foreign key constraints
        commentRepository.deleteByProjectId(id);
        attachmentRepository.deleteByProjectId(id);
        bugRepository.deleteByProjectId(id);
        testingExecutionRepository.deleteByProjectId(id);
        testCaseRepository.deleteByProjectId(id);
        featureRepository.deleteByProjectId(id);
        projectRepository.delete(project);

        return "Project '" + projectName + "' and all associated features, test cases, executions, and bugs deleted successfully";
    }
}
