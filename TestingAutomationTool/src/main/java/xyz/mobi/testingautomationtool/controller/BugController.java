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
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.dto.BugDto.*;
import xyz.mobi.testingautomationtool.dto.BugDto.GetBugResponse.BugResponse;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.BugService;

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
    public ResponseEntity<BugResponse> createBug(@Valid @RequestBody BugRequest request) {
        BugResponse response = bugService.createBug(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bugId}")
    public ResponseEntity<BugResponse> getById(@PathVariable Integer bugId) {
        return ResponseEntity.ok(bugService.getById(bugId));
    }

    @GetMapping
    public ResponseEntity<Page<BugResponse>> getAllBugs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResponseEntity.ok(bugService.getAllBugs(page, size));
    }

    @PutMapping("/{bugId}")
    public ResponseEntity<BugResponse> updateBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPutRequest request) {
        return ResponseEntity.ok(bugService.updateBug(bugId, request));
    }

    @PatchMapping("/{bugId}")
    public ResponseEntity<String> patchBug(
            @PathVariable Integer bugId,
            @Valid @RequestBody BugPatchRequest request) {
        return ResponseEntity.ok(bugService.patchBug(bugId, request));
    }

    @PatchMapping("/delete/{bugId}")
    public ResponseEntity<Void> deleteBug(@PathVariable Integer bugId) {
        bugService.deleteBug(bugId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{bugId}")
    public ResponseEntity<Void> hardDeleteBug(@PathVariable Integer bugId) {
        bugService.hardDelete(bugId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<BugResponse>> globalSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BugSeverity severity,
            @RequestParam(required = false) BugPriority priority,
            @RequestParam(required = false) BugStatus status,
            @RequestParam(required = false) BugCategory category,
            @RequestParam(required = false) Integer bugOccurrence,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) LocalDate resolvedFrom,
            @RequestParam(required = false) String executedBy,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false) String updatedBy,
            @RequestParam(required = false) LocalDate resolvedTo,
            @RequestParam(required = false) String timeZone,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            @ParameterObject Pageable pageable
    ) {
        Page<BugResponse> response = bugService.globalSearch(
                keyword, severity, priority, status, category,
                bugOccurrence, isActive, resolvedFrom, resolvedTo,
                timeZone, pageable, executedBy, assignedTo, updatedBy
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{bugId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Integer bugId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "attachmentType", required = false, defaultValue = "BUG") AttachmentType attachmentType,
            @RequestParam("uploadedBy") Integer uploadedBy) {
        AttachmentResponse response = attachmentService.uploadBugAttachment(bugId, file, attachmentType, uploadedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{bugId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(@PathVariable Integer bugId) {
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByBugId(bugId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable Integer attachmentId) {
        AttachmentDownloadResponse attachment = bugService.downloadBugAttachments(attachmentId);
        String contentType = attachment.getContentType() != null ? attachment.getContentType(): MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFile());
    }
}