package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.service.AttachmentService;


@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
@Tag(name = "Attachment Management", description = "Common attachment operations for bugs, features, projects, and test cases")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<String> deleteAttachment(
            @PathVariable Integer attachmentId) {

        return ResponseEntity.ok(
                attachmentService.deleteAttachment(attachmentId)
        );
    }
}
