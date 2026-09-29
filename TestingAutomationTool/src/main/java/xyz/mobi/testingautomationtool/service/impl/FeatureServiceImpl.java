package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    @CacheEvict(value = "features", key = "#featureId")
    public FeatureResponse patchFeature(Integer featureId, FeaturePatchRequest request) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (request.getLockVersion() != null &&
                !request.getLockVersion().equals(feature.getVersion())) {
            throw new CustomException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }

        if (feature.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        // Validate feature name uniqueness within the same project if changed
        if (request.getFeatureName() != null && !request.getFeatureName().isBlank() &&
                !request.getFeatureName().equals(feature.getFeatureName())) {
            Integer projectId = feature.getProject() != null ? feature.getProject().getProjectId() : null;
            if (projectId != null && featureRepository.existsByProject_ProjectIdAndFeatureName(projectId, request.getFeatureName())) {
                throw new CustomException(ErrorCode.DUPLICATE_RESOURCE);
            }
            feature.setFeatureName(request.getFeatureName().trim());
        }

        if (request.getDescription() != null) {
            feature.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            feature.setStatus(request.getStatus());
        }
        if (request.getDuration() != null) {
            feature.setDuration(request.getDuration());
        }
        if (request.getSprint() != null) {
            feature.setSprint(request.getSprint());
        }
        if (request.getVersion() != null) {
            feature.setFeatureVersion(request.getVersion());
        }
        if (request.getStartTime() != null) {
            feature.setStartTime(request.getStartTime());
        }
        if (request.getIsActive() != null) {
            feature.setActive(request.getIsActive());
        }
        if (request.getUpdatedBy() != null) {
            User updater = userRepository.findById(request.getUpdatedBy())
                    .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
            feature.setUpdatedBy(updater);
        }

        Feature saved = featureRepository.save(feature);
        return toResponse(saved);
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
        }

        featureRepository.save(feature);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "features", key = "#featureId")
    public FeatureResponse getFeatureById(Integer featureId) {
        Feature feature = featureRepository.findByFeatureIdAndIsDeletedFalse(featureId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
        return toResponse(feature);
    }
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<FeatureResponse> getFeaturesByProjectId(Integer projectId) {
//        return featureRepository.findByProject_ProjectIdAndIsDeletedFalse(projectId).stream()
//                .map(this::toResponse)
//                .toList();
//    }

    private FeatureResponse toResponse(Feature f) {
        return FeatureResponse.builder()
                .featureId(f.getFeatureId())
                .projectId(f.getProject() != null ? f.getProject().getProjectId() : null)
                .projectName(f.getProject() != null ? f.getProject().getProjectName() : null)
                .featureName(f.getFeatureName())
                .description(f.getDescription())
                .status(f.getStatus())
                .duration(f.getDuration())
                .sprint(f.getSprint())
                .version(f.getFeatureVersion())
                .startTime(f.getStartTime())
                .isActive(f.isActive())
                .createdBy(f.getCreatedBy() != null ? f.getCreatedBy().getUserId() : null)
                .updatedBy(f.getUpdatedBy() != null ? f.getUpdatedBy().getUserId() : null)
                .createdAt(f.getCreatedAt())
                .updatedAt(f.getUpdatedAt())
                .lockVersion(f.getVersion())
                .build();
    }
}
