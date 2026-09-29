package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureService featureService;

   @GetMapping("/{featureId}")
   public ResponseEntity<FeatureResponse> getFeatureById(@PathVariable Integer featureId) {
       return ResponseEntity.ok(featureService.getFeatureById(featureId));
   }
//
//    @GetMapping("/project/{projectId}")
//    public ResponseEntity<List<FeatureResponse>> getFeaturesByProjectId(@PathVariable Integer projectId) {
//        return ResponseEntity.ok(featureService.getFeaturesByProjectId(projectId));
//    }

    @PatchMapping("/{featureId}")
    public ResponseEntity<FeatureResponse> patchFeature(
            @PathVariable Integer featureId,
            @RequestBody FeaturePatchRequest request) {
        return ResponseEntity.ok(featureService.patchFeature(featureId, request));
    }

    @DeleteMapping("/{featureId}")
    public ResponseEntity<Void> deleteFeature(
            @PathVariable Integer featureId,
            @RequestParam(required = false) Integer updatedBy) {
        featureService.deleteFeature(featureId, updatedBy);
        return ResponseEntity.noContent().build();
    }
}
