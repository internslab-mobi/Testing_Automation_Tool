package xyz.mobi.testingautomationtool.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDto.*;

import java.io.IOException;
import java.util.List;

public interface FeatureService {
    FeatureResponse createFeature(FeatureRequest request);

    FeatureResponse getFeatureById(Integer featureId);

    List<FeatureResponse> getFeaturesByProjectId(Integer projectId);

    List<FeatureResponse> searchFeatures(FeatureSearchRequest request);

    FeaturePutResponse updateFeature(Integer featureId, FeaturePutRequest request);

    FeaturePatchResponse patchFeature(Integer featureId, FeaturePatchRequest request);

    void deleteFeature(Integer featureId, Integer updatedBy);

    AttachmentResponse uploadAttachment(MultipartFile file, Integer featureId) throws IOException;

    ResponseEntity<byte[]> downloadFile(Integer featureId);

    FeatureStartTimeResponse startFeature(Integer featureId);

    FeatureDurationResponse endFeature(Integer featureId);
}
