package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.*;

import java.io.IOException;
import java.util.List;

public interface FeatureService {

    FeatureResponse createFeature(FeatureRequest request);

    FeatureResponse getFeatureById(Integer featureId);

    List<FeatureResponse> getFeaturesByProjectId(Integer projectId);

    Page<FeatureResponse> searchFeatures(
            FeatureSearchRequest request,
            Pageable pageable
    );

    FeaturePutResponse updateFeature(Integer featureId, FeaturePutRequest request);

    String patchFeature(Integer featureId, FeaturePatchRequest request);

    void deleteFeature(Integer featureId, Integer updatedBy);

    AttachmentResponse uploadAttachment(MultipartFile file, Integer featureId) throws IOException;

    @Transactional(readOnly = true)
    AttachmentDownloadResponse downloadFiles(Integer featureId);
}
