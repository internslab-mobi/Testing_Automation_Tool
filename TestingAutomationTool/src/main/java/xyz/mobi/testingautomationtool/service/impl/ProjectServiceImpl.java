package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.ProjectRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.ProjectResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.DuplicateResourceException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.postMapper.ProjectMapper;
import xyz.mobi.testingautomationtool.repository.AttachmentRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.ProjectService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final AttachmentRepository attachmentRepository;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {

        String projectName = request.getProjectName().trim();

        if (projectRepository.existsByProjectNameIgnoreCase(projectName)) {
            throw new DuplicateResourceException(
                    "Project already exists with name: " + projectName
            );
        }

        Integer currentUserId = 1;

        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found with ID: " + currentUserId
                        )
                );

        Project project = projectMapper.toEntity(request);

        project.setProjectName(projectName);
        project.setCreatedBy(createdBy);
        project.setStatus(ProjectStatus.ACTIVE);
        project.setActive(true);
        project.setDeleted(false);
        project.setUpdatedBy(null);

        Project savedProject = projectRepository.save(project);

        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    @Override
    public AttachmentResponse uploadProjectDocument(
            MultipartFile file,
            Integer projectId) throws IOException {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project is not present for this id: "
                                        + projectId));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Document file is required");
        }

        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();
        Long fileSize = file.getSize();
        byte[] bytes = file.getBytes();

        User user = userRepository.findById(2)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User is not found"));

        Attachment attachment = Attachment.builder()
                .attachmentType(AttachmentType.PROJECT)
                .project(project)
                .fileName(fileName)
                .fileType(fileType)
                .fileSize(fileSize)
                .fileBlob(bytes)
                .uploadedBy(user)
                .build();

        attachmentRepository.save(attachment);

        return AttachmentResponse.builder()
                .contentType(fileType)
                .fileSize(fileSize)
                .fileName(fileName)
                .createdAt(attachment.getCreatedAt())
                .projectId(projectId)
                .build();
    }
}
