package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.featureRequest.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.response.featureResponse.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.entity.Project;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.featureMapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FeatureMapper featureMapper;
    private final FeatureRepository featureRepository;
    @Override
    public FeatureResponse createFeature(FeatureRequest request)  {
        Project project = projectRepository.findById(request.getProjectId()).orElseThrow(()->new ResourceNotFoundException("Project is not available for this id:"+request.getProjectId()));
        User user = userRepository.findById(request.getCreatedBy()).orElseThrow(()->new ResourceNotFoundException("User not found for this id"+request.getCreatedBy()));
        Feature feature = featureMapper.toEntity(request);
        feature.setProject(project);
        feature.setCreatedBy(user);
        featureRepository.save(feature);
        return featureMapper.toResponse(feature);
    }


}
