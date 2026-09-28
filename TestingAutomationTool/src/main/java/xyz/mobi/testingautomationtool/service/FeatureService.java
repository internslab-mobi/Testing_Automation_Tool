package xyz.mobi.testingautomationtool.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;

import java.io.File;
import java.io.IOException;

public interface FeatureService  {
    public FeatureResponse createFeature(FeatureRequest request);

    public AttachmentResponse uploadAttachment(MultipartFile file, Integer featureId) throws IOException;

    public ResponseEntity<byte[]> downloadFile(Integer featureId);

    public FeatureStartTimeResponse startFeature(Integer featureId);

    public FeatureDurationResponse endFeature(Integer featureId);

    public FeaturePutResponse updateFeature(
            Integer featureId,
            FeaturePutRequest request);
}
