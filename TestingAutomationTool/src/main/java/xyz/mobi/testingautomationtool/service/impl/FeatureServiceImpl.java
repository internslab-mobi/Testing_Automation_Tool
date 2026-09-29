package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public FeaturePatchResponse patchFeature(
            Integer featureId,
            FeaturePatchRequest request) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // Don't allow updating deleted feature
        if (feature.isDeleted()) {
            throw new CustomException(
                    ErrorCode.RESOURCE_NOT_FOUND);
        }

        // Stores names of fields that were actually changed
        List<String> updatedFields = new ArrayList<>();

        // Feature name
        if (request.getFeatureName() != null &&
                !request.getFeatureName().isBlank() &&
                !request.getFeatureName().equals(feature.getFeatureName())) {

            Integer projectId = feature.getProject() != null
                    ? feature.getProject().getProjectId()
                    : null;

            if (projectId != null &&
                    featureRepository.existsByProject_ProjectIdAndFeatureName(
                            projectId,
                            request.getFeatureName())) {

                throw new CustomException(
                        ErrorCode.DUPLICATE_RESOURCE);
            }

            feature.setFeatureName(
                    request.getFeatureName().trim());

            updatedFields.add("Feature name");
        }

        // Description
        if (request.getDescription() != null &&
                !request.getDescription().equals(feature.getDescription())) {

            feature.setDescription(request.getDescription());
            updatedFields.add("Description");
        }

        // Status
        if (request.getStatus() != null &&
                !request.getStatus().equals(feature.getStatus())) {

            feature.setStatus(request.getStatus());
            updatedFields.add("Status");
        }

        // Duration
        if (request.getDuration() != null &&
                !request.getDuration().equals(feature.getDuration())) {

            feature.setDuration(request.getDuration());
            updatedFields.add("Duration");
        }

        // Sprint
        if (request.getSprint() != null &&
                !request.getSprint().equals(feature.getSprint())) {

            feature.setSprint(request.getSprint());
            updatedFields.add("Sprint");
        }

        // Version
        if (request.getVersion() != null &&
                !request.getVersion().equals(feature.getFeatureVersion())) {

            feature.setFeatureVersion(request.getVersion());
            updatedFields.add("Version");
        }

        // Start time
        if (request.getStartTime() != null &&
                !request.getStartTime().equals(feature.getStartTime())) {

            feature.setStartTime(request.getStartTime());
            updatedFields.add("Start time");
        }

        // Active status
        if (request.getIsActive() != null &&
                !request.getIsActive().equals(feature.isActive())) {

            feature.setActive(request.getIsActive());
            updatedFields.add("Active status");
        }

        // Updated by
        if (request.getUpdatedBy() != null) {

            User updater = userRepository.findById(
                    request.getUpdatedBy()
            ).orElseThrow(() ->
                    new CustomException(
                            ErrorCode.RESOURCE_NOT_FOUND));

            feature.setUpdatedBy(updater);
        }

        // Save changes
        featureRepository.save(feature);

        // Build success message
        String message;

        if (updatedFields.isEmpty()) {
            message = "No fields were updated";
        } else if (updatedFields.size() == 1) {
            message = updatedFields.get(0)
                    + " updated successfully";
        } else {
            message = String.join(", ", updatedFields)
                    + " updated successfully";
        }

        return FeaturePatchResponse.builder()
                .message(message)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public void deleteFeature(
            Integer featureId,
            Integer updatedBy) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.RESOURCE_NOT_FOUND));

        if (feature.isDeleted()) {
            throw new CustomException(
                    ErrorCode.RESOURCE_NOT_FOUND);
        }

        feature.setDeleted(true);
        feature.setActive(false);

        if (updatedBy != null) {

            User updater = userRepository.findById(updatedBy)
                    .orElse(null);

            feature.setUpdatedBy(updater);
        }

        featureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "features", key = "#featureId")
    public FeatureResponse getFeatureById(
            Integer featureId) {

        Feature feature = featureRepository
                .findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() ->
                        new CustomException(
                                ErrorCode.RESOURCE_NOT_FOUND));

        return toResponse(feature);
    }

    private FeatureResponse toResponse(Feature f) {

        return FeatureResponse.builder()
                .featureId(f.getFeatureId())
                .projectId(
                        f.getProject() != null
                                ? f.getProject().getProjectId()
                                : null
                )
                .projectName(
                        f.getProject() != null
                                ? f.getProject().getProjectName()
                                : null
                )
                .featureName(f.getFeatureName())
                .description(f.getDescription())
                .status(f.getStatus())
                .duration(f.getDuration())
                .sprint(f.getSprint())
                .version(f.getFeatureVersion())
                .startTime(f.getStartTime())
                .isActive(f.isActive())
                .createdBy(
                        f.getCreatedBy() != null
                                ? f.getCreatedBy().getUserId()
                                : null
                )
                .updatedBy(
                        f.getUpdatedBy() != null
                                ? f.getUpdatedBy().getUserId()
                                : null
                )
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .lockVersion(f.getVersion())
                .build();
    }
}