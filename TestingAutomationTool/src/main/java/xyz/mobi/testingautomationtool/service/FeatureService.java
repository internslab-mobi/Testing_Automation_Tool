package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;

import java.util.List;
import java.io.File;
import java.io.IOException;

public interface FeatureService {
    FeatureResponse patchFeature(Integer featureId, FeaturePatchRequest request);
    void deleteFeature(Integer featureId, Integer updatedBy);
    FeatureResponse getFeatureById(Integer featureId);
    List<FeatureResponse> getFeaturesByProjectId(Integer projectId);
    public FeatureResponse createFeature(FeatureRequest request);

    public AttachmentResponse uploadAttachment(MultipartFile file, Integer featureId) throws IOException;

    public ResponseEntity<byte[]> downloadFile(Integer featureId);

    public FeatureStartTimeResponse startFeature(Integer featureId);

    public FeatureDurationResponse endFeature(Integer featureId);

    public FeaturePutResponse updateFeature(
            Integer featureId,
            FeaturePutRequest request);

}
