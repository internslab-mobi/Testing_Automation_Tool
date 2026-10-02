package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDto.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.AttachmentRepository;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.FeatureService;
import xyz.mobi.testingautomationtool.specification.FeatureSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
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
public class FeatureServiceImpl implements FeatureService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FeatureMapper featureMapper;
    private final FeatureRepository featureRepository;
    private final AttachmentRepository attachmentRepository;
    private final AuthService authService;

    @Override
    public FeatureResponse createFeature(FeatureRequest request) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not available for id: " + request.getProjectId()));


        User user = authService.getCurrentUser();

        if (featureRepository.existsByProject_ProjectIdAndFeatureName(project.getProjectId(), request.getFeatureName())) {
            throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
        }

        Feature feature = featureMapper.toEntity(request);
        feature.setStartTime(null);
        feature.setProject(project);
        feature.setCreatedBy(user);
        feature.setUpdatedBy(user);
        feature.setActive(true);
        feature.setDeleted(false);

        Feature saved = featureRepository.save(feature);
        return featureMapper.toResponse(saved);
    }

    @Override
    public AttachmentResponse uploadAttachment(
            MultipartFile file,
            Integer featureId) throws IOException {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feature is not present for this id: " + featureId));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            filename = "feature_attachment_" + Instant.now().toEpochMilli();
        }

        boolean exists = attachmentRepository
                .existsByFeature_FeatureIdAndFileNameAndIsDeletedFalseAndIsActiveTrue(
                        featureId, filename
                );

        if (exists) {
            throw new ResourceNotFoundException(
                    "File already exists in this feature: " + filename
            );
        }

        String fileType = file.getContentType();
        Long fileSize = file.getSize();
        byte[] bytes = file.getBytes();

        User user = authService.getCurrentUser();

        Attachment attachment = Attachment.builder()
                .attachmentType(AttachmentType.FEATURE)
                .feature(feature)
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

        return AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .attachmentType(AttachmentType.FEATURE)
                .featureId(featureId)
                .fileName(filename)
                .fileType(fileType)
                .fileSize(fileSize)
                .uploadedBy(user != null ? user.getUserId() : null)
                .createdAt(attachment.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public String patchFeature(Integer featureId, FeaturePatchRequest request) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (feature.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        List<String> updatedFields = new ArrayList<>();

        if (request.getFeatureName() != null &&
                !request.getFeatureName().isBlank() &&
                !request.getFeatureName().equals(feature.getFeatureName())) {

            Integer projectId = feature.getProject() != null ? feature.getProject().getProjectId() : null;
            if (projectId != null && featureRepository.existsByProject_ProjectIdAndFeatureName(projectId, request.getFeatureName())) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
            }

            feature.setFeatureName(request.getFeatureName().trim());
            updatedFields.add("featureName");
        }

        if (request.getDescription() != null && !request.getDescription().equals(feature.getDescription())) {
            feature.setDescription(request.getDescription());
            updatedFields.add("description");
        }

        if (request.getStatus() != null && !request.getStatus().equals(feature.getStatus())) {
            feature.setStatus(request.getStatus());
            updatedFields.add("status");
        }

        if (request.getSprint() != null && !request.getSprint().equals(feature.getSprint())) {
            feature.setSprint(request.getSprint());
            updatedFields.add("sprint");
        }

        if (request.getFeatureVersion() != null && !request.getFeatureVersion().equals(feature.getFeatureVersion())) {
            feature.setFeatureVersion(request.getFeatureVersion());
            updatedFields.add("featureVersion");
        } else if (request.getVersion() != null && !request.getVersion().equals(feature.getFeatureVersion())) {
            feature.setFeatureVersion(request.getVersion());
            updatedFields.add("version");
        }

        if (request.getDuration() != null && !request.getDuration().equals(feature.getDuration())) {
            feature.setDuration(request.getDuration());
            updatedFields.add("duration");
        }

        if (request.getStartTime() != null && !request.getStartTime().equals(feature.getStartTime())) {
            feature.setStartTime(request.getStartTime());
            updatedFields.add("startTime");
        }

        if (Boolean.TRUE.equals(request.getStartTimer())) {
            feature.setStartTime(Instant.now());
            updatedFields.add("startTimer");
        }

        if (Boolean.TRUE.equals(request.getEndTimer())) {
            if (feature.getStartTime() != null) {
                Instant endTime = Instant.now();
                long elapsedSeconds = Duration.between(feature.getStartTime(), endTime).getSeconds();
                long currentDuration = feature.getDuration() != null ? feature.getDuration() : 0L;
                long totalDuration = currentDuration + elapsedSeconds;
                feature.setDuration(totalDuration);
                feature.setStartTime(null);
            }
            updatedFields.add("endTimer");
        }

        if (request.getIsActive() != null && !request.getIsActive().equals(feature.isActive())) {
            feature.setActive(request.getIsActive());
            updatedFields.add("isActive");
        }

        if (request.getUpdatedBy() != null) {
            User updater = userRepository.findById(request.getUpdatedBy())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
            feature.setUpdatedBy(updater);
        } else {
            try {
                feature.setUpdatedBy(authService.getCurrentUser());
            } catch (Exception ignored) {
            }
        }

        if (updatedFields.isEmpty()) {
            throw new IllegalArgumentException("At least one field must be provided for update");
        }

        featureRepository.save(feature);

        return "Feature with ID " + featureId + " updated successfully. Changed fields: " + String.join(", ", updatedFields);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public void deleteFeature(Integer featureId, Integer updatedBy) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (feature.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        feature.setDeleted(true);
        feature.setActive(false);

        if (updatedBy != null) {
            User updater = userRepository.findById(updatedBy).orElse(null);
            feature.setUpdatedBy(updater);
        } else {
            try {
                feature.setUpdatedBy(authService.getCurrentUser());
            } catch (Exception ignored) {
            }
        }

        featureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "features", key = "#featureId")
    public FeatureResponse getFeatureById(Integer featureId) {
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        return featureMapper.toResponse(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeaturesByProjectId(Integer projectId) {
        return featureRepository.findByProject_ProjectIdAndIsDeletedFalse(projectId).stream()
                .map(featureMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> searchFeatures(FeatureSearchRequest request) {
        return featureRepository.findAll(FeatureSpecification.search(request)).stream()
                .map(featureMapper::toResponse)
                .toList();
    }


    private String formatDuration(long totalSeconds) {
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        if (days > 0) {
            result.append(days).append(days == 1 ? " day " : " days ");
        }
        if (hours > 0) {
            result.append(hours).append(hours == 1 ? " hour " : " hours ");
        }
        if (minutes > 0) {
            result.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
        }
        if (seconds > 0) {
            result.append(seconds).append(seconds == 1 ? " second" : " seconds");
        }
        if (result.isEmpty()) {
            return "0 seconds";
        }
        return result.toString().trim();
    }

    @Override
    public FeaturePutResponse updateFeature(Integer featureId, FeaturePutRequest request) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + featureId));

        featureMapper.updateEntityFromPut(feature, request);
        try {
            feature.setUpdatedBy(authService.getCurrentUser());
        } catch (Exception ignored) {
        }

        Feature savedFeature = featureRepository.save(feature);
        return featureMapper.toPutResponse(savedFeature);
    }


    @Transactional(readOnly = true)
    @Override
    public AttachmentDownloadResponse downloadFiles(Integer featureId) {

        List<Attachment> attachments =
                attachmentRepository
                        .findAllByFeature_FeatureIdAndIsDeletedFalseAndIsActiveTrue(featureId);

        if (attachments.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No attachments found for feature ID: " + featureId
            );
        }

        if (attachments.size() == 1) {
            Attachment attachment = attachments.getFirst();

            if (attachment.getFileBlob() == null) {
                throw new AttachmentProcessingException(
                        "File content is missing"
                );
            }

            return AttachmentDownloadResponse.builder()
                    .file(attachment.getFileBlob())
                    .fileName(attachment.getFileName())
                    .contentType(attachment.getFileType())
                    .build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            Set<String> fileNames = new HashSet<>();

            for (Attachment attachment : attachments) {

                String fileName = attachment.getFileName();
                byte[] fileBlob = attachment.getFileBlob();

                if (fileName == null || fileName.isBlank()) {
                    continue;
                }

                // Skip duplicate filenames
                if (!fileNames.add(fileName)) {
                    continue;
                }

                if (fileBlob == null) {
                    continue;
                }

                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                zos.write(fileBlob);
                zos.closeEntry();
            }

            zos.finish();
            return AttachmentDownloadResponse.builder()
                    .file(baos.toByteArray())
                    .fileName("feature_" + featureId + "_attachments.zip")
                    .contentType("application/zip")
                    .build();

        } catch (IOException e) {

            throw new AttachmentProcessingException(
                    "Failed to create ZIP file for feature ID: " + featureId

            );
        }
    }
}
