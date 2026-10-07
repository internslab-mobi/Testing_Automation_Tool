package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
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

    String deleteFeature(Integer featureId);

    String hardDeleteFeature(Integer featureId);

//    @Transactional(readOnly = true)
//    AttachmentDownloadResponse downloadFiles(Integer featureId);
}
