package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.*;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.*;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.FeatureStatus;
import xyz.mobi.testingautomationtool.enums.ProjectStatus;
import xyz.mobi.testingautomationtool.exception.AttachmentProcessingException;
import xyz.mobi.testingautomationtool.exception.DuplicateResourceException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.*;
import xyz.mobi.testingautomationtool.service.AuthService;
import xyz.mobi.testingautomationtool.service.FeatureService;
import xyz.mobi.testingautomationtool.specification.FeatureSpecification;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.Exception;
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
    private final BugRepository bugRepository;
    private final TestCaseRepository testCaseRepository;
    private final AuthService authService;

    @Override
    public FeatureResponse createFeature(FeatureRequest request) {
//            Project project = projectRepository.findByProjectIdAndStatusAndIsActiveTrueAndIsDeletedFalse(request.getProjectId(),
//                            ProjectStatus.ACTIVE)
        Project project = projectRepository.findByProjectIdAndStatusAndIsActiveTrueAndIsDeletedFalse(request.getProjectId(),
                        ProjectStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Project not available for id: " + request.getProjectId()));


        User user = authService.getCurrentUser();

        if (featureRepository.existsByProject_ProjectIdAndFeatureName(project.getProjectId(), request.getFeatureName())) {
            throw new DuplicateResourceException("Feature is already existed");
        }

        Feature feature = featureMapper.toEntity(request);
        feature.setStatus(FeatureStatus.ACTIVE);
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
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public String patchFeature(Integer featureId, FeaturePatchRequest request) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + featureId));

        if (feature.isDeleted()) {
            throw new ResourceNotFoundException("Feature not found with ID: " + featureId);
        }

        List<String> updatedFields = new ArrayList<>();

        if (request.getFeatureName() != null &&
                !request.getFeatureName().isBlank() &&
                !request.getFeatureName().equals(feature.getFeatureName())) {

            Integer projectId = feature.getProject() != null ? feature.getProject().getProjectId() : null;
            if (projectId != null && featureRepository.existsByProject_ProjectIdAndFeatureName(projectId, request.getFeatureName())) {
                throw new DuplicateResourceException("Feature name already exists in this project: " + request.getFeatureName());
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

            if (feature.getStartTime() == null) {
                throw new IllegalStateException("Start time is null");
            }

            Instant endTime = Instant.now();

            long elapsedSeconds =
                    Duration.between(feature.getStartTime(), endTime).getSeconds();

            long currentDuration =
                    feature.getDuration() != null
                            ? feature.getDuration()
                            : 0L;

            feature.setDuration(currentDuration + elapsedSeconds);
            feature.setStartTime(null);

            updatedFields.add("endTimer");
        }

        if (request.getIsActive() != null && !request.getIsActive().equals(feature.isActive())) {
            feature.setActive(request.getIsActive());
            updatedFields.add("isActive");
        }

        if (request.getUpdatedBy() != null) {
            User updater = userRepository.findById(request.getUpdatedBy())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUpdatedBy()));
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

        return "Feature with ID " + featureId +
                " updated successfully. Changed fields: " +
                String.join(", ", updatedFields);
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public String deleteFeature(Integer featureId) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found with ID: " + featureId));

        if (feature.isDeleted()) {
            throw new ResourceNotFoundException(
                    "Feature not found with ID: " + featureId);
        }

        feature.setDeleted(true);
        feature.setActive(false);


        User currentUser = authService.getCurrentUser();
        feature.setUpdatedBy(currentUser);

        featureRepository.save(feature);
        bugRepository.deactivateBugsByProjectId(featureId);
        attachmentRepository.deactivateAttachmentsByProjectId(featureId);
        testCaseRepository.deactivateTestCasesByProjectId(featureId);

        return "Feature with ID " + featureId + " deleted successfully.";
    }

    public String hardDeleteFeature(Integer featureId) {
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found with ID: " + featureId));

        featureRepository.delete(feature);

        return "Feature with ID " + featureId + " deleted successfully.";
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "features", key = "#featureId")
    public FeatureResponse getFeatureById(Integer featureId) {
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalseAndProjectStatus(
                        featureId,
                        ProjectStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with ID: " + featureId));

        return featureMapper.toResponse(feature);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureResponse> getFeaturesByProjectId(Integer projectId) {
        return featureRepository.findByProject_ProjectIdAndIsDeletedFalseAndIsActiveTrueAndProject_Status(
                        projectId,
                        ProjectStatus.ACTIVE)
                .stream()
                .map(featureMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FeatureResponse> searchFeatures(
            FeatureSearchRequest request,
            Pageable pageable) {

        return featureRepository
                .findAll(FeatureSpecification.search(request), pageable)
                .map(featureMapper::toResponse);
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
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalseAndProjectStatus(
                featureId,
                        ProjectStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Feature not found with id: " + featureId));

        if(feature.isDeleted() || !feature.isActive()){
            throw new IllegalStateException("Cannot update disabled/deleted feature with ID: " + featureId);
        }
        featureMapper.updateEntityFromPut(feature,request);

        feature.setUpdatedBy(authService.getCurrentUser());

        Feature savedFeature = featureRepository.save(feature);
        return featureMapper.toPutResponse(savedFeature);
    }


//    @Transactional(readOnly = true)
//    @Override
//    public AttachmentDownloadResponse downloadFiles(Integer featureId) {
//
//        List<Attachment> attachments =
//                attachmentRepository
//                        .findAllByFeature_FeatureIdAndIsDeletedFalseAndIsActiveTrue(featureId);
//
//        if (attachments.isEmpty()) {
//            throw new ResourceNotFoundException(
//                    "No attachments found for feature ID: " + featureId
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
//                    .fileName("feature_" + featureId + "_attachments.zip")
//                    .contentType("application/zip")
//                    .build();
//
//        } catch (IOException e) {
//
//            throw new AttachmentProcessingException(
//                    "Failed to create ZIP file for feature ID: " + featureId
//
//            );
//        }
//    }
}
