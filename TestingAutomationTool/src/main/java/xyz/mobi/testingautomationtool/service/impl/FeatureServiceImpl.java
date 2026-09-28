package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.getmethoddto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.entity.Feature;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.getMapper.FeatureMapper;
import xyz.mobi.testingautomationtool.repository.FeatureRepository;
import xyz.mobi.testingautomationtool.repository.ProjectRepository;
import xyz.mobi.testingautomationtool.service.FeatureService;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {

    private final FeatureRepository featureRepository;
    private final ProjectRepository projectRepository;
    private final FeatureMapper featureMapper;

    @Cacheable(value = "features", key = "#featureId")
    @Override
    @Transactional(readOnly = true)
    public FeatureResponse getFeatureById(Integer featureId) {

        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Feature not found with ID: " + featureId
                        ));

        return featureMapper.toResponse(feature);
    }

    @Cacheable(
            value = "featuresByProject",
            key = "#projectId + '_' + #pageable"
    )
    @Override
    @Transactional(readOnly = true)
    public Page<FeatureResponse> getFeaturesByProject(
            Integer projectId,
            Pageable pageable) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: " + projectId
                        ));

        return featureRepository
                .findByProject_ProjectId(projectId, pageable)
                .map(featureMapper::toResponse);
    }

    @Cacheable(
            value = "featureSearch",
            key = "#request + '_' + #pageable"
    )
    @Override
    @Transactional(readOnly = true)
    public Page<FeatureResponse> searchFeatures(
            FeatureSearchRequest request,
            Pageable pageable) {

        Specification<Feature> specification =
                FeatureSpecification.search(request);

        return featureRepository
                .findAll(specification, pageable)
                .map(featureMapper::toResponse);
    }
}
