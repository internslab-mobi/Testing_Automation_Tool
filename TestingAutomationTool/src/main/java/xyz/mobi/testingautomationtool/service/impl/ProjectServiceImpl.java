package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.ProjectDTO.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.DuplicateResourceException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.ProjectMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.ProjectService;
import xyz.mobi.testingautomationtool.specification.ProjectSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private final ProjectMapper projectMapper;
    private final AuthService authService;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        if (projectRepository.existsByProjectNameAndRegion(request.getProjectName(), request.getRegion())) {
            throw new DuplicateResourceException("Project already exists with name: " + request.getProjectName() + " and region: " + request.getRegion());
        }

        User user = authService.getCurrentUser();
        Project project = projectMapper.toEntity(request);
        project.setCreatedBy(user);
        project.setUpdatedBy(user);
        project.setActive(true);
        project.setDeleted(false);
        project.setStatus(ProjectStatus.ACTIVE);

        Project saved = projectRepository.save(project);
        return projectMapper.toResponse(saved);
    }

//    @Override
//    @Transactional
//    public AttachmentResponse uploadAttachment(
//            MultipartFile file,
//            Integer projectId) throws IOException {
//
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Project is not present for this id: " + projectId
//                ));
//
//        String filename = file.getOriginalFilename();
//
//        if (filename == null || filename.isBlank()) {
//            filename = "Project_attachment_" + Instant.now().toEpochMilli();
//        }
//
//        // Check duplicate filename within the same project
//        boolean exists = attachmentRepository
//                .existsByProject_ProjectIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(
//                        projectId, filename
//                );
//
//        if (exists) {
//            throw new ResourceNotFoundException(
//                    "File already exists in this project: " + filename
//            );
//        }
//
//        String fileType = file.getContentType();
//        Long fileSize = file.getSize();
//        byte[] bytes = file.getBytes();
//
//        User user = authService.getCurrentUser();
//
//        Attachment attachment = Attachment.builder()
//                .attachmentType(AttachmentType.PROJECT)
//                .project(project)
//                .fileName(filename)
//                .fileType(fileType)
//                .fileSize(fileSize)
//                .fileBlob(bytes)
//                .uploadedBy(user)
//                .updatedBy(user)
//                .isActive(true)
//                .isDeleted(false)
//                .build();
//
//        attachmentRepository.save(attachment);
//
//        return AttachmentResponse.builder()
//                .attachmentId(attachment.getAttachmentId())
//                .attachmentType(AttachmentType.PROJECT)
//                .projectId(projectId)
//                .fileName(filename)
//                .fileType(fileType)
//                .fileSize(fileSize)
//                .uploadedBy(user != null ? user.getUserId() : null)
//                .createdAt(attachment.getCreatedAt())
//                .build();
//    }

    @Transactional
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
    @Transactional
    public String patchProject(Integer id, ProjectPatchRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Patch request cannot be null");
        }

        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + id
                        )
                );

        if (project.isDeleted() || !project.isActive()) {
            throw new IllegalStateException(
                    "Project is permanently deleted and cannot be modified with ID or contact to manager: " + id
            );
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

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field must be provided for update"
            );
        }

        project.setUpdatedBy(currentUser);

        projectRepository.save(project);

        return "Project with ID " + id +
                " updated successfully. Changed fields: " +
                String.join(", ", updatedFields);
    }


    @Transactional
    @Override
    public String patchProjectActiveStatus(Integer id, ProjectStatus isActive) {

        if (isActive == null) {
            throw new IllegalArgumentException(
                    "isActive cannot be null"
            );
        }
        Project project = projectRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + id
                        )
                );


        if (project.isDeleted()) {
            throw new IllegalStateException(
                    "Project is permanently deleted cannot be modified" + id
            );
        }
        if (isActive == project.getStatus()) {
            throw new IllegalStateException("Project is already in same status:" + id);
        }

        User currentUser = authService.getCurrentUser();

        if (currentUser == null ||
                currentUser.getRole() == null ||
                currentUser.getRole().getRole() == null ||
                !currentUser.getRole().getRole()
                        .equalsIgnoreCase("MANAGER")) {

            throw new AccessDeniedException(
                    "Only MANAGER can change project active status"
            );
        }
        boolean condition = isActive == ProjectStatus.ACTIVE;
        project.setActive(condition);

        project.setStatus(
                isActive
        );

        if (condition) {

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

        return "Project with ID " + id +
                " active status changed to " + isActive +
                " successfully";
        }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findByIsDeletedFalse()
                .stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProjectById(Integer projectId) {
        Project project = projectRepository.findByProjectIdAndIsDeletedFalse(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> searchProjects(
            String keyword,
            ProjectStatus status) {

        Specification<Project> spec =
                ProjectSpecification.search(keyword, status);

        return projectRepository.findAll(spec).stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Transactional
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

    @Transactional
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

//    @Override
//    @Transactional(readOnly = true)
//    public AttachmentDownloadResponse downloadFiles(Integer projectId) {
//
//        List<Attachment> attachments =
//                attachmentRepository
//                        .findAllByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrue(projectId);
//
//        if (attachments.isEmpty()) {
//            throw new ResourceNotFoundException(
//                    "No attachments found for project ID: " + projectId
//            );
//        }
//
//        if (attachments.size() == 1) {
//            Attachment attachment = attachments.getFirst();
//
//            if (attachment.getFileBlob() == null) {
//                throw new AttachmentProcessingException(
//                        "File content is missing"
//                );
//            }
//
//            return AttachmentDownloadResponse.builder()
//                    .file(attachment.getFileBlob())
//                    .fileName(attachment.getFileName())
//                    .contentType(attachment.getFileType())
//                    .build();
//        }
//
//        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             ZipOutputStream zos = new ZipOutputStream(baos)) {
//
//            Set<String> fileNames = new HashSet<>();
//
//            for (Attachment attachment : attachments) {
//
//                String fileName = attachment.getFileName();
//                byte[] fileBlob = attachment.getFileBlob();
//
//                if (fileName == null || fileName.isBlank()) {
//                    continue;
//                }
//
//                // Skip duplicate filenames
//                if (!fileNames.add(fileName)) {
//                    continue;
//                }
//
//                if (fileBlob == null) {
//                    continue;
//                }
//
//                ZipEntry zipEntry = new ZipEntry(fileName);
//                zos.putNextEntry(zipEntry);
//                zos.write(fileBlob);
//                zos.closeEntry();
//            }
//
//            zos.finish();
//            return AttachmentDownloadResponse.builder()
//                    .file(baos.toByteArray())
//                    .fileName("project_" + projectId + "_attachments.zip")
//                    .contentType("application/zip")
//                    .build();
//
//        } catch (IOException e) {
//
//            throw new AttachmentProcessingException(
//                    "Failed to create ZIP file for project ID: " + projectId
//
//            );
//        }
//    }
}
