package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.repository.AttachmentRepository;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.AuthService;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final FeatureRepository featureRepository;
    private final ProjectRepository projectRepository;

    @Transactional
    public List<AttachmentResponse> uploadAttachments(
            Integer parentId,
            List<MultipartFile> files,
            AttachmentType attachmentType) throws IOException {

        // 1. Validate request
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }

        if (attachmentType == null) {
            throw new IllegalArgumentException("Attachment type cannot be null");
        }

        User user = authService.getCurrentUser();

        // 2. Validate parent entity
        Bug bug = null;
        Feature feature = null;
        Project project = null;

        switch (attachmentType) {

            case BUG -> bug = bugRepository.findByBugIdAndIsActiveTrueAndIsDeletedFalse(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Bug is not present for this id: " + parentId));

            case FEATURE -> feature = featureRepository.findByFeatureIdAndIsActiveTrueAndIsDeletedFalse(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Feature is not present for this id: " + parentId));

            case PROJECT -> project = projectRepository.findByProjectIdAndIsActiveTrueAndIsDeletedFalse(parentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Project is not present for this id: " + parentId));

            default -> throw new IllegalArgumentException(
                    "Unsupported attachment type: " + attachmentType);
        }

        List<Attachment> attachments = new ArrayList<>();
        Set<String> uploadedFileNames = new HashSet<>();

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException(
                        "Uploaded file cannot be empty");
            }

            String filename = file.getOriginalFilename();

            if (filename == null || filename.isBlank()) {
                filename = "attachment_" + UUID.randomUUID();
            }

            if (!uploadedFileNames.add(filename)) {
                throw new IllegalArgumentException(
                        "Duplicate filename in upload: " + filename);
            }

            boolean exists;

            // Duplicate in database
            switch (attachmentType) {

                case BUG ->  exists = attachmentRepository.existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(parentId, filename);

                case FEATURE -> exists = attachmentRepository.existsByFeature_FeatureIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(parentId, filename);

                case PROJECT -> exists = attachmentRepository.existsByProject_ProjectIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(parentId, filename);

                default -> throw new IllegalArgumentException(
                        "Unsupported attachment type: " + attachmentType);
            }

            if (exists) {
                throw new IllegalArgumentException(
                        "File already exists: " + filename);
            }

            Attachment.AttachmentBuilder builder = Attachment.builder()
                    .attachmentType(attachmentType)
                    .fileName(filename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileBlob(file.getBytes())
                    .uploadedBy(user)
                    .updatedBy(user)
                    .isActive(true)
                    .isDeleted(false);

            // Attach the correct parent
            switch (attachmentType) {
                case BUG -> builder.bug(bug);
                case FEATURE -> builder.feature(feature);
                case PROJECT -> builder.project(project);
            }

            attachments.add(builder.build());
        }

        // 4. Save
        List<Attachment> savedAttachments =
                attachmentRepository.saveAll(attachments);

        // 5. Response
        return savedAttachments.stream()
                .map(attachment -> AttachmentResponse.builder()
                        .attachmentId(attachment.getAttachmentId())
                        .bugId(attachment.getBug() != null ? attachment.getBug().getBugId() : null)
                        .featureId(attachment.getFeature() != null ? attachment.getFeature().getFeatureId() : null)
                        .projectId(attachment.getProject() != null ? attachment.getProject().getProjectId() : null)
                        .attachmentType(attachment.getAttachmentType())
                        .fileName(attachment.getFileName())
                        .fileType(attachment.getFileType())
                        .fileSize(attachment.getFileSize())
                        .uploadedBy(
                                attachment.getUploadedBy() != null
                                        ? attachment.getUploadedBy().getUserId()
                                        : null
                        )
                        .createdAt(attachment.getCreatedAt())
                        .build()
                )
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByBugId(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (bug.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return attachmentRepository.findByBug_BugIdAndIsDeletedFalse(bugId).stream()
                .map(this::toResponse)
                .toList();
    }


    private AttachmentResponse toResponse(Attachment a) {
        return AttachmentResponse.builder()
                .attachmentId(a.getAttachmentId())
                .bugId(a.getBug() != null ? a.getBug().getBugId() : null)
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .uploadedBy(a.getUploadedBy() != null ? a.getUploadedBy().getUserId() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
