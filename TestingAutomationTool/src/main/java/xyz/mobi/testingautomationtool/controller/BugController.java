package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.BugDTO.*;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.BugService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class BugController {

    private final BugService bugService;
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<BugResponse>> createBug(
            @RequestParam Integer testcaseId,
            @Valid @RequestBody BugRequest request) {
        BugResponse response = bugService.createBug(testcaseId,request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bug created successfully", response));
    }

    @GetMapping("/{bugId}")
    public ResponseEntity<ApiResponse<BugResponse>> getById(@PathVariable Integer bugId) {
        return ResponseEntity.ok(ApiResponse.success("Bug retrieved successfully", bugService.getById(bugId)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BugResponse>>> getAllBugs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(ApiResponse.success("Bugs retrieved successfully", bugService.getAllBugs(page, size)));
    }

    @PutMapping("/{bugId}")
    public ResponseEntity<ApiResponse<BugResponse>> updateBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPutRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Bug updated successfully", bugService.updateBug(bugId, request)));
    }

    @PatchMapping("/{bugId}")
    public ResponseEntity<ApiResponse<String>> patchBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPatchRequest request) {
        return ResponseEntity.ok(ApiResponse.success(bugService.patchBug(bugId, request)));
    }

    @DeleteMapping("/delete/{bugId}")
    public ResponseEntity<ApiResponse<String>> deleteBug(@PathVariable Integer bugId) {
        bugService.deleteBug(bugId);
        return ResponseEntity.ok(ApiResponse.success("Bug soft deleted successfully"));
    }

    @DeleteMapping("/{bugId}")
    public ResponseEntity<ApiResponse<String>> hardDeleteBug(@PathVariable Integer bugId) {
        bugService.hardDelete(bugId);
        return ResponseEntity.ok(ApiResponse.success("Bug permanently deleted successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BugResponse>>> globalSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BugSeverity severity,
            @RequestParam(required = false) BugPriority priority,
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) BugCategory category,
            @RequestParam(required = false) Integer bugOccurrence,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) LocalDate resolvedFrom,
            @RequestParam(required = false) LocalDate resolvedTo,
            @RequestParam(required = false, defaultValue = "UTC") String timeZone,
            @RequestParam(required = false) String executedBy,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String updatedBy,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable) {

        return ResponseEntity.ok(
                ApiResponse.success("Bug search results", bugService.globalSearch(keyword, severity, priority, status, category,
                        bugOccurrence, isActive, resolvedFrom, resolvedTo, timeZone, pageable, executedBy, assignedTo, updatedBy))
        );
    }

    @PostMapping(value = "/attachment/{bugId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> uploadAttachment(
            @RequestParam("file") List<MultipartFile> file,
            @PathVariable("bugId") Integer bugId
    ) throws IOException {
        List<AttachmentResponse> response = attachmentService.uploadAttachments(AttachmentType.BUG, bugId, file);
        return ResponseEntity.ok(ApiResponse.success("Attachments uploaded successfully", response));
    }

    @GetMapping("/bugs/{bugId}/attachments/download")
    public ResponseEntity<byte[]> downloadBugAttachments(
            @PathVariable Integer bugId) {

        AttachmentDownloadResponse response =
                bugService.downloadBugAttachments(bugId);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition.attachment()
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