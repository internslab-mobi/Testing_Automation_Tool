package xyz.mobi.testingautomationtool.service;

import org.springframework.http.ResponseEntity;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;

public interface FeatureService  {
    public FeatureResponse createFeature( FeatureRequest request);

    public ResponseEntity<byte[]> downloadFile(Integer featureId);

    public FeatureStartTimeResponse startFeature(Integer featureId);

    public FeatureDurationResponse endFeature(Integer featureId);

    public FeaturePutResponse updateFeature(
            Integer featureId,
            FeaturePutRequest request);
}
