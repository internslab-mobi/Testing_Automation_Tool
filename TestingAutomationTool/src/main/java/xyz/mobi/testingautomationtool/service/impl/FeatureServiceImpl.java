package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.FileProcessingException;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.featureMapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.AttachmentRepository;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FeatureServiceImpl implements FeatureService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FeatureMapper featureMapper;
    private final FeatureRepository featureRepository;
    private final AttachmentRepository attachmentRepository;
    @Override
    public FeatureResponse createFeature(FeatureRequest request)  {

        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(
                ()->new ResourceNotFoundException
                        ("Project is not available for this id:"+request.getProjectId()));

        User user = userRepository.findById(request.getCreatedBy()).orElseThrow(
                ()->new ResourceNotFoundException
                        ("User not found for this id"+request.getCreatedBy()));


        Feature feature = featureMapper.toEntity(request);

        feature.setStartTime(null);

        feature.setProject(project);

        feature.setCreatedBy(user);

        return featureMapper.toResponse(featureRepository.save(feature));
    }

    @Override
    public AttachmentResponse uploadAttachment(MultipartFile file, Integer featureId) throws IOException {


        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(()->new ResourceNotFoundException("Feature is not present for this id:"+featureId));
        String filename = file.getOriginalFilename();

        String fileType = file.getContentType();

        Long fileSize = file.getSize();

        byte[] bytes = file.getBytes();

        User user = userRepository.findById(2).orElseThrow(()->new ResourceNotFoundException("User is not found"));

        Attachment attachment = Attachment.builder()
                .attachmentType(AttachmentType.FEATURE)
                .feature(feature)
                .fileName(filename)
                .fileType(fileType)
                .fileSize(fileSize)
                .fileBlob(bytes)
                .uploadedBy(user)
                .build();

        attachmentRepository.save(attachment);

        return  AttachmentResponse.builder()
                .contentType(fileType)
                .fileSize(fileSize)
                .fileName(filename)
                .createdAt(attachment.getCreatedAt())
                .featureId(featureId)
                .build();
    }


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

    @Override
    public ResponseEntity<byte[]> downloadFile(Integer featureId) {

        Feature feature = featureRepository.findById(featureId).orElseThrow(()-> new ResourceNotFoundException("Feature data is not present"));

        return null;
//                ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
//                "attachment; filename=\"" + feature.getTestcaseFileName()+ "\"")
//                .contentType(MediaType.parseMediaType(feature.getTestcaseFileType()))
//                .body(feature.getTestcaseFile());
    }

    @Override
    public FeatureStartTimeResponse startFeature(Integer featureId) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found: " + featureId
                        ));

        Instant startTime = Instant.now();

        feature.setStartTime(startTime);

        featureRepository.save(feature);

        return FeatureStartTimeResponse.builder()
                .startTime(startTime)
                .build();
    }
    @Override
    public FeatureDurationResponse endFeature(Integer featureId) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found: " + featureId
                        ));

        if (feature.getStartTime() == null) {
            return FeatureDurationResponse.builder()
                    .duration(formatDuration(feature.getDuration()))
                    .build();
        }

        LocalDateTime endTime = LocalDateTime.now();

        long elapsedSeconds = Duration.between(
                feature.getStartTime(),
                endTime
        ).getSeconds();

        long totalDuration =
                feature.getDuration() + elapsedSeconds;

        feature.setDuration(totalDuration);
        feature.setStartTime(null);

        featureRepository.save(feature);

        return FeatureDurationResponse.builder()
                .duration(formatDuration(totalDuration))
                .build();
    }


    private String formatDuration(long totalSeconds) {

        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days)
                    .append(days == 1 ? " day " : " days ");
        }

        if (hours > 0) {
            result.append(hours)
                    .append(hours == 1 ? " hour " : " hours ");
        }

        if (minutes > 0) {
            result.append(minutes)
                    .append(minutes == 1 ? " minute " : " minutes ");
        }

        if (seconds > 0) {
            result.append(seconds)
                    .append(seconds == 1 ? " second" : " seconds");
        }

        if (result.isEmpty()) {
            return "0 seconds";
        }

        return result.toString().trim();
    }
    @Override
    @Transactional
    public FeaturePutResponse updateFeature(
            Integer featureId,
            FeaturePutRequest request) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found with id: " + featureId));

        feature.setFeatureName(request.getFeatureName());
        feature.setDescription(request.getDescription());
        feature.setStatus(request.getStatus());
        feature.setSprint(request.getSprint());
        feature.setFeatureVersion(request.getVersion());

        Feature savedFeature = featureRepository.save(feature);

        return FeaturePutResponse.builder()
                .featureId(savedFeature.getFeatureId())
                .projectId(savedFeature.getProject().getProjectId())
                .featureName(savedFeature.getFeatureName())
                .description(savedFeature.getDescription())
                .status(savedFeature.getStatus())
                .sprint(savedFeature.getSprint())
                .version(savedFeature.getFeatureVersion())
                .duration(savedFeature.getDuration())
                .startTime(savedFeature.getStartTime())
                .createdBy(savedFeature.getCreatedBy().getUserId())
                .build();
    }
}
