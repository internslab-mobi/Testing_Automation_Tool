package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
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

    @PostMapping
    public ResponseEntity<FeatureResponse> createFeature(
            @Valid @RequestBody FeatureRequest featureRequest) {
        FeatureResponse featureResponse = featureService.createFeature(featureRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(featureResponse);
    }

    @GetMapping("/{featureId}")
    public ResponseEntity<FeatureResponse> getFeatureById(
            @PathVariable Integer featureId) {
        return ResponseEntity.ok(featureService.getFeatureById(featureId));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<FeatureResponse>> getFeaturesByProjectId(
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(featureService.getFeaturesByProjectId(projectId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<FeatureResponse>> searchFeatures(
            @ModelAttribute FeatureSearchRequest request,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                featureService.searchFeatures(request, pageable)
        );
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<FeaturePutResponse> updateFeature(
            @PathVariable Integer featureId,
            @Valid @RequestBody FeaturePutRequest request) {
        return ResponseEntity.ok(featureService.updateFeature(featureId, request));
    }

    @PatchMapping("/{featureId}")
    public ResponseEntity<String> patchFeature(
            @PathVariable Integer featureId,
            @RequestBody FeaturePatchRequest request) {
        return ResponseEntity.ok(featureService.patchFeature(featureId, request));
    }

    @DeleteMapping("/{featureId}")
    public ResponseEntity<String> deleteFeature(
            @PathVariable Integer featureId,
            @RequestParam(required = false) Integer updatedBy) {
        featureService.deleteFeature(featureId, updatedBy);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body("The feature has been deleted");
    }

    @PostMapping(value = "/attachment/{featureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @PathVariable("featureId") Integer featureId
    ) throws IOException {
        AttachmentResponse featureResponse = featureService.uploadAttachment(file, featureId);
        return ResponseEntity.ok(featureResponse);
    }

    @GetMapping("/features/{featureId}/attachments/download")
    public ResponseEntity<byte[]> downloadfeatureAttachments(
            @PathVariable Integer featureId) {

        AttachmentDownloadResponse response =
                featureService.downloadFiles(featureId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(response.getFileName())
                                .build()
                                .toString()
                )
                .contentType(
                        MediaType.parseMediaType(response.getContentType())
                )
                .contentLength(response.getFile().length)
                .body(response.getFile());
    }
}
