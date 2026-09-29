package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;

import java.util.List;

public interface FeatureService {
    FeatureResponse patchFeature(Integer featureId, FeaturePatchRequest request);
    void deleteFeature(Integer featureId, Integer updatedBy);
    FeatureResponse getFeatureById(Integer featureId);
    // List<FeatureResponse> getFeaturesByProjectId(Integer projectId);
}
