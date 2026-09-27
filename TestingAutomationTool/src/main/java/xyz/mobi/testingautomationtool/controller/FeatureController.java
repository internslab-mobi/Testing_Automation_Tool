package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

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

    @GetMapping("/feature/{id}/file")
    public ResponseEntity<byte[]> displayFile(@PathVariable("id")Integer featureId){

        return featureService.downloadFile(featureId);

    }

    @PatchMapping("/{featureId}/start")
    public ResponseEntity<FeatureStartTimeResponse> startFeature(
            @PathVariable Integer featureId) {

        FeatureStartTimeResponse response =
                featureService.startFeature(featureId);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{featureId}/end")
    public ResponseEntity<FeatureDurationResponse> endFeature(
            @PathVariable Integer featureId) {

        FeatureDurationResponse response =
                featureService.endFeature(featureId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<FeaturePutResponse> updateFeature(
            @PathVariable Integer featureId,
            @Valid @RequestBody FeaturePutRequest request) {

        FeaturePutResponse response =
                featureService.updateFeature(featureId, request);

        return ResponseEntity.ok(response);
    }


}