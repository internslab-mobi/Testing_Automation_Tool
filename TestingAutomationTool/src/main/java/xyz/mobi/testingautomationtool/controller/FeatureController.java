package xyz.mobi.testingautomationtool.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.FeatureRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.FeaturePutRequest;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureDurationResponse;
import xyz.mobi.testingautomationtool.dto.response.patchmethodDTO.FeatureStartTimeResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.dto.response.putMethodDTO.FeaturePutResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchRequest;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeaturePatchResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.FeatureResponse;
import xyz.mobi.testingautomationtool.service.FeatureService;

import java.util.List;
import java.io.IOException;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

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

    @PostMapping( value = "/create")
    public ResponseEntity<FeatureResponse> createFeatureDetails(
            @Valid @RequestBody FeatureRequest featureRequest) {

        FeatureResponse featureResponse = featureService.createFeature(featureRequest);

        return ResponseEntity.ok(featureResponse);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FeatureResponse>> getFeaturesByProjectId(@PathVariable Integer projectId) {
        return ResponseEntity.ok(featureService.getFeaturesByProjectId(projectId));
        }

    @PostMapping(value = "/attachment/{featureId}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(@RequestBody MultipartFile file,@PathVariable("featureId") Integer featureId
    ) throws IOException {

        AttachmentResponse featureResponse = featureService.uploadAttachment(file,featureId);

        return ResponseEntity.ok(featureResponse);
    }
}
