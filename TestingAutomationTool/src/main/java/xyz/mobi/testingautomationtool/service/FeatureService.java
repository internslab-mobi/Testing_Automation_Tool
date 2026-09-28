package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mobi.testingautomationtool.dto.request.getmethoddto.FeatureSearchRequest;
import xyz.mobi.testingautomationtool.dto.response.getMethodDTO.FeatureResponse;

public interface FeatureService {

    FeatureResponse getFeatureById(Integer featureId);

    Page<FeatureResponse> getFeaturesByProject(
            Integer projectId,
            Pageable pageable
    );

    Page<FeatureResponse> searchFeatures(
            FeatureSearchRequest request,
            Pageable pageable
    );
}
