package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.service.AttachmentService;

import java.util.List;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
@Tag(name = "Attachment Management", description = "Common attachment operations for bugs, features, projects, and test cases")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/{attachmentType}/{entityId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload attachments", description = "Upload multiple files for a parent entity")
    public ResponseEntity<List<AttachmentResponse>> uploadAttachments(
            @Parameter(description = "Attachment type: BUG, FEATURE, PROJECT, TESTCASE") @PathVariable AttachmentType attachmentType,
            @Parameter(description = "Parent entity ID") @PathVariable Integer entityId,
            @RequestParam("files") List<MultipartFile> files) {

        List<AttachmentResponse> response = attachmentService.uploadAttachments(attachmentType, entityId, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{attachmentType}/{entityId}")
    @Operation(summary = "Get attachments", description = "Retrieve all active attachments metadata for a parent entity")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @Parameter(description = "Attachment type: BUG, FEATURE, PROJECT, TESTCASE") @PathVariable AttachmentType attachmentType,
            @Parameter(description = "Parent entity ID") @PathVariable Integer entityId) {

        List<AttachmentResponse> attachments = attachmentService.getAttachments(attachmentType, entityId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentType}/{entityId}/download")
    @Operation(summary = "Download entity attachments", description = "Download parent entity attachments as a single file or a ZIP archive")
    public ResponseEntity<byte[]> downloadAttachments(
            @Parameter(description = "Attachment type: BUG, FEATURE, PROJECT, TESTCASE") @PathVariable AttachmentType attachmentType,
            @Parameter(description = "Parent entity ID") @PathVariable Integer entityId) {

        AttachmentDownloadResponse attachment = attachmentService.downloadAttachments(attachmentType, entityId);
        String contentType = attachment.getContentType() != null && !attachment.getContentType().isBlank()
                ? attachment.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFile());
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(
            summary = "Delete attachment",
            description = "Soft delete an attachment"
    )
    public ResponseEntity<String> deleteAttachment(
            @Parameter(description = "Attachment ID")
            @PathVariable Integer attachmentId) {

        String message = attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.ok(message);
    }
}
