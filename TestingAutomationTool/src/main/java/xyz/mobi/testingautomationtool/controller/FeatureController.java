package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDto.*;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/feature")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class FeatureController {

    private final FeatureService featureService;

    @GetMapping("/{featureId}")
    public ResponseEntity<FeatureResponse> getFeatureById(@PathVariable Integer featureId) {
        return ResponseEntity.ok(featureService.getFeatureById(featureId));
    }

    @PatchMapping("/{featureId}")
    public ResponseEntity<FeaturePatchResponse> patchFeature(
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

    @PostMapping(value = "/create")
    public ResponseEntity<FeatureResponse> createFeatureDetails(
            @Valid @RequestBody FeatureRequest featureRequest) {
        FeatureResponse featureResponse = featureService.createFeature(featureRequest);
        return ResponseEntity.ok(featureResponse);
    }

    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(
            @Valid @RequestBody FeatureRequest featureRequest) {
        FeatureResponse featureResponse = featureService.createFeature(featureRequest);
        return ResponseEntity.ok(featureResponse);
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<FeaturePutResponse> updateFeature(
            @PathVariable Integer featureId,
            @Valid @RequestBody FeaturePutRequest request) {
        return ResponseEntity.ok(featureService.updateFeature(featureId, request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FeatureResponse>> getFeaturesByProjectId(@PathVariable Integer projectId) {
        return ResponseEntity.ok(featureService.getFeaturesByProjectId(projectId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FeatureResponse>> searchFeatures(FeatureSearchRequest request) {
        return ResponseEntity.ok(featureService.searchFeatures(request));
    }

    @PatchMapping("/{featureId}/start")
    public ResponseEntity<FeatureStartTimeResponse> startFeature(@PathVariable Integer featureId) {
        return ResponseEntity.ok(featureService.startFeature(featureId));
    }

    @PatchMapping("/{featureId}/end")
    public ResponseEntity<FeatureDurationResponse> endFeature(@PathVariable Integer featureId) {
        return ResponseEntity.ok(featureService.endFeature(featureId));
    }

    @PostMapping(value = "/attachment/{featureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @PathVariable("featureId") Integer featureId
    ) throws IOException {
        AttachmentResponse featureResponse = featureService.uploadAttachment(file, featureId);
        return ResponseEntity.ok(featureResponse);
    }

    @GetMapping("/attachment/{featureId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Integer featureId) {
        return featureService.downloadFile(featureId);
    }
}
