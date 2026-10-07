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
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.FeatureDTO.*;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.service.AttachmentService;
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
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeatureResponse>> createFeature(
            @Valid @RequestBody FeatureRequest featureRequest) {
        FeatureResponse featureResponse = featureService.createFeature(featureRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feature created successfully", featureResponse));
    }

    @GetMapping("/{featureId}")
    public ResponseEntity<ApiResponse<FeatureResponse>> getFeatureById(
            @PathVariable Integer featureId) {
        return ResponseEntity.ok(ApiResponse.success("Feature retrieved successfully", featureService.getFeatureById(featureId)));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<FeatureResponse>>> getFeaturesByProjectId(
            @PathVariable Integer projectId) {
        return ResponseEntity.ok(ApiResponse.success("Features retrieved successfully", featureService.getFeaturesByProjectId(projectId)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<FeatureResponse>>> searchFeatures(
            @ModelAttribute FeatureSearchRequest request,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success("Features search results", featureService.searchFeatures(request, pageable))
        );
    }

    @PutMapping("/{featureId}")
    public ResponseEntity<ApiResponse<FeaturePutResponse>> updateFeature(
            @PathVariable Integer featureId,
            @Valid @RequestBody FeaturePutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Feature updated successfully", featureService.updateFeature(featureId, request)));
    }

    @PatchMapping("/{featureId}")
    public ResponseEntity<ApiResponse<String>> patchFeature(
            @PathVariable Integer featureId,
            @RequestBody FeaturePatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(featureService.patchFeature(featureId, request)));
    }

    @DeleteMapping("/{featureId}")
    public ResponseEntity<ApiResponse<String>> deleteFeature(
            @PathVariable Integer featureId,
            @RequestParam(required = false) Integer updatedBy) {
        featureService.deleteFeature(featureId, updatedBy);
        return ResponseEntity.ok(ApiResponse.success("The feature has been deleted"));
    }

    @PostMapping(value = "/attachment/{featureId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadAttachment(
            @RequestParam("file") List<MultipartFile> file,
            @PathVariable("featureId") Integer featureId
    ) throws IOException {
        List<AttachmentResponse> featureResponse = attachmentService.uploadAttachments(AttachmentType.FEATURE, featureId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachments uploaded successfully", featureResponse));
    }

    @GetMapping("/features/{featureId}/attachments/download")
    public ResponseEntity<byte[]> downloadfeatureAttachments(
            @PathVariable Integer featureId) {

        AttachmentDownloadResponse response = featureService.downloadFiles(featureId);

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
