package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.featureRequest.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.response.featureResponse.FeatureResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.io.IOException;

@RestController
@RequestMapping("/feature")
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureService featureService;
    @PostMapping( value = "/create" )
    public ResponseEntity<FeatureResponse> createFeatureDetails(
            @Valid @RequestBody FeatureRequest featureRequest) {
        FeatureResponse featureResponse = featureService.createFeature(featureRequest);
        return ResponseEntity.ok(featureResponse);
    }
}