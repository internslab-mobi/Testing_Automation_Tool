package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.service.AttachmentService;

@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN', 'DEVELOPER')")
@Tag(name = "Attachment Management", description = "Common attachment operations for bugs, features, projects, and test cases")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping("/{attachmentId:\\d+}/download")
    public ResponseEntity<ByteArrayResource> downloadSingleAttachment(@PathVariable("attachmentId") Integer attachmentId) {
        AttachmentDownloadResponse response = attachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + response.getFileName() + "\"")
                .body(new ByteArrayResource(response.getFile()));
    }

    @GetMapping("/{attachmentId:\\d+}/view")
    public ResponseEntity<ByteArrayResource> viewSingleAttachment(@PathVariable("attachmentId") Integer attachmentId) {
        AttachmentDownloadResponse response = attachmentService.getAttachmentById(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(response.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + response.getFileName() + "\"")
                .body(new ByteArrayResource(response.getFile()));
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(
            @PathVariable Integer attachmentId) {

        return ResponseEntity.ok(
                attachmentService.deleteAttachment(attachmentId)
        );
    }
}
