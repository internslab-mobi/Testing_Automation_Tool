package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.*;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.AttachmentMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.AuthService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final BugRepository bugRepository;
    private final FeatureRepository featureRepository;
    private final ProjectRepository projectRepository;
    private final TestCaseRepository testCaseRepository;
    private final AuthService authService;
    private final AttachmentMapper attachmentMapper;

    @Override
    @Transactional
    public List<AttachmentResponse> uploadAttachments(
            AttachmentType type,
            Integer entityId,
            List<MultipartFile> files) {

        if (type == null) {
            throw new IllegalArgumentException("Attachment type cannot be null");
        }
        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException("Entity ID must be a positive integer");
        }
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Files list cannot be null or empty");
        }

        Bug bug = null;
        Feature feature = null;
        Project project = null;
        TestCase testCase = null;

        switch (type) {
            case BUG -> bug = bugRepository.findById(entityId)
                    .filter(b -> !b.isDeleted() && b.isActive())
                    .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + entityId));
            case FEATURE -> feature = featureRepository.findById(entityId)
                    .filter(f -> !f.isDeleted() && f.isActive())
                    .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + entityId));
            case PROJECT -> project = projectRepository.findById(entityId)
                    .filter(p -> !p.isDeleted() && p.isActive())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + entityId));
            case TESTCASE -> testCase = testCaseRepository.findById(entityId)
                    .filter(t -> !t.isDeleted() && t.isActive())
                    .orElseThrow(() -> new ResourceNotFoundException("TestCase not found with ID: " + entityId));
        }

        User user;

        try {
            user = authService.getCurrentUser();
        } catch (Exception e) {
            log.error("Could not determine current authenticated user", e);
            throw new AttachmentProcessingException(
                    "Could not determine the current authenticated user", e);
        }

        Set<String> requestFileNames = new HashSet<>();
        List<Attachment> attachmentsToSave = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Uploaded file cannot be null or empty");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                throw new IllegalArgumentException("File name cannot be empty");
            }

            String cleanFileName = StringUtils.cleanPath(originalFilename);
            if (cleanFileName.contains("..")) {
                throw new IllegalArgumentException("File name contains invalid path sequence: " + originalFilename);
            }

            if (!requestFileNames.add(cleanFileName)) {
                throw new IllegalArgumentException("Duplicate filename in upload request: " + cleanFileName);
            }

            boolean existsInDb = switch (type) {
                case BUG -> attachmentRepository.existsByBug_BugIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(entityId, cleanFileName);
                case FEATURE -> attachmentRepository.existsByFeature_FeatureIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(entityId, cleanFileName);
                case PROJECT -> attachmentRepository.existsByProject_ProjectIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(entityId, cleanFileName);
                case TESTCASE -> attachmentRepository.existsByTestCase_TestcaseIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(entityId, cleanFileName);
            };

            if (existsInDb) {
                throw new IllegalArgumentException("File already exists for " + type + " with ID " + entityId + ": " + cleanFileName);
            }

            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                log.error("Failed to read bytes for file '{}': {}", cleanFileName, e.getMessage());
                throw new AttachmentProcessingException("Failed to read file content for: " + cleanFileName);
            }

            String fileType = file.getContentType();
            if (fileType == null || fileType.isBlank()) {
                fileType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            Attachment attachment = Attachment.builder()
                    .attachmentType(type)
                    .fileName(cleanFileName)
                    .fileType(fileType)
                    .fileSize(file.getSize())
                    .fileBlob(bytes)
                    .uploadedBy(user)
                    .updatedBy(user)
                    .isActive(true)
                    .isDeleted(false)
                    .build();

            switch (type) {
                case BUG -> attachment.setBug(bug);
                case FEATURE -> attachment.setFeature(feature);
                case PROJECT -> attachment.setProject(project);
                case TESTCASE -> attachment.setTestCase(testCase);
            }

            attachmentsToSave.add(attachment);
        }

        List<Attachment> saved = attachmentRepository.saveAll(attachmentsToSave);
        return saved.stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachments(AttachmentType type, Integer entityId) {
        if (type == null) {
            throw new IllegalArgumentException("Attachment type cannot be null");
        }
        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException("Entity ID must be a positive integer");
        }

        validateEntityExists(type, entityId);

        List<Attachment> attachments = switch (type) {
            case BUG -> attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case FEATURE -> attachmentRepository.findAllByFeature_FeatureIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case PROJECT -> attachmentRepository.findAllByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case TESTCASE -> attachmentRepository.findAllByTestCase_TestcaseIdAndIsDeletedFalseAndIsActiveTrue(entityId);
        };

        return attachments.stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentDownloadResponse downloadAttachments(AttachmentType type, Integer entityId) {
        if (type == null) {
            throw new IllegalArgumentException("Attachment type cannot be null");
        }
        if (entityId == null || entityId <= 0) {
            throw new IllegalArgumentException("Entity ID must be a positive integer");
        }

        validateEntityExists(type, entityId);

        List<Attachment> attachments = switch (type) {
            case BUG -> attachmentRepository.findAllByBug_BugIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case FEATURE -> attachmentRepository.findAllByFeature_FeatureIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case PROJECT -> attachmentRepository.findAllByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrue(entityId);
            case TESTCASE -> attachmentRepository.findAllByTestCase_TestcaseIdAndIsDeletedFalseAndIsActiveTrue(entityId);
        };

        List<Attachment> valid = attachments.stream()
                .filter(a -> a.getFileName() != null && !a.getFileName().isBlank() && a.getFileBlob() != null && a.getFileBlob().length > 0)
                .toList();

        if (valid.isEmpty()) {
            throw new ResourceNotFoundException("No attachments found for " + type + " ID: " + entityId);
        }

        if (valid.size() == 1) {
            Attachment single = valid.get(0);
            String contentType = single.getFileType() != null && !single.getFileType().isBlank()
                    ? single.getFileType()
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE;

            return AttachmentDownloadResponse.builder()
                    .file(single.getFileBlob())
                    .fileName(single.getFileName())
                    .contentType(contentType)
                    .build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Set<String> entryNames = new HashSet<>();
            for (Attachment a : valid) {
                String baseName = a.getFileName();
                String entryName = baseName;
                int count = 1;
                while (!entryNames.add(entryName)) {
                    int dotIdx = baseName.lastIndexOf('.');
                    if (dotIdx != -1) {
                        entryName = baseName.substring(0, dotIdx) + " (" + count + ")" + baseName.substring(dotIdx);
                    } else {
                        entryName = baseName + " (" + count + ")";
                    }
                    count++;
                }

                ZipEntry zipEntry = new ZipEntry(entryName);
                zos.putNextEntry(zipEntry);
                zos.write(a.getFileBlob());
                zos.closeEntry();
            }
            zos.finish();

            String zipFileName = type.name().toLowerCase() + "-" + entityId + "-attachments.zip";
            return AttachmentDownloadResponse.builder()
                    .file(baos.toByteArray())
                    .fileName(zipFileName)
                    .contentType("application/zip")
                    .build();

        } catch (IOException e) {
            log.error("Failed to package attachments into ZIP for {} ID {}: {}", type, entityId, e.getMessage());
            throw new AttachmentProcessingException("Failed to package attachments into ZIP for " + type + " ID: " + entityId);
        }
    }

    private void validateEntityExists(AttachmentType type, Integer entityId) {

        switch (type) {

            case BUG -> bugRepository.findById(entityId)
                    .filter(b -> !b.isDeleted() && b.isActive())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Bug not found or inactive with ID: " + entityId));

            case FEATURE -> featureRepository.findById(entityId)
                    .filter(f -> !f.isDeleted() && f.isActive())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Feature not found or inactive with ID: " + entityId));

            case PROJECT -> projectRepository.findById(entityId)
                    .filter(p -> !p.isDeleted() && p.isActive())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Project not found or inactive with ID: " + entityId));

            case TESTCASE -> testCaseRepository.findById(entityId)
                    .filter(t -> !t.isDeleted() && t.isActive())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "TestCase not found or inactive with ID: " + entityId));
        }
    }

    @Override
    @Transactional
    public String deleteAttachment(Integer attachmentId) {

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Attachment not found with ID: " + attachmentId));

        if (attachment.isDeleted()) {
            throw new ResourceNotFoundException(
                    "Attachment not found with ID: " + attachmentId);
        }

        attachment.setDeleted(true);
        attachment.setActive(false);

        attachmentRepository.save(attachment);

        return "Attachment '" + attachment.getFileName() + "' deleted successfully.";
    }
}
