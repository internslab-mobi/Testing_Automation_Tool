package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.featureMapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;
import org.springframework.transaction.annotation.Transactional;
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

        featureRepository.save(feature);

        return featureMapper.toResponse(feature);
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
        feature.setVersion(request.getVersion());

        Feature savedFeature = featureRepository.save(feature);

        return FeaturePutResponse.builder()
                .featureId(savedFeature.getFeatureId())
                .projectId(savedFeature.getProject().getProjectId())
                .featureName(savedFeature.getFeatureName())
                .description(savedFeature.getDescription())
                .status(savedFeature.getStatus())
                .sprint(savedFeature.getSprint())
                .version(savedFeature.getVersion())
                .duration(savedFeature.getDuration())
                .startTime(savedFeature.getStartTime())
                .createdBy(savedFeature.getCreatedBy().getUserId())
                .build();
    }
}




