package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.featureRequest.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.response.featureResponse.FeatureResponse;

import java.io.IOException;

public interface FeatureService  {
    public FeatureResponse createFeature( FeatureRequest request);
}
