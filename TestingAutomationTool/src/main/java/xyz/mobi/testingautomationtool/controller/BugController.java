package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.enums.BugCategory;
import xyz.mobi.testingautomationtool.enums.BugPriority;
import xyz.mobi.testingautomationtool.enums.BugSeverity;
import xyz.mobi.testingautomationtool.enums.BugStatus;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugRequest;
import xyz.mobi.testingautomationtool.dto.BugDTO.BugResponse;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.service.AttachmentService;
import xyz.mobi.testingautomationtool.service.BugService;
import xyz.mobi.testingautomationtool.service.CommentService;

import java.util.List;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/bugs")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class BugController {

    private final BugService bugService;
    private final CommentService commentService;
    private final AttachmentService attachmentService;

    @PostMapping
    public ResponseEntity<BugResponse> createBug(
            @Valid @RequestBody BugRequest request) {

        BugResponse response = bugService.createBug(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{bugId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable Integer bugId,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse response = commentService.addComment(bugId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{bugId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Integer bugId) {

        List<CommentResponse> comments = commentService.getCommentsByBugId(bugId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping(value = "/{bugId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @PathVariable Integer bugId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "attachmentType", required = false, defaultValue = "BUG") AttachmentType attachmentType,
            @RequestParam("uploadedBy") Integer uploadedBy) {

        AttachmentResponse response = attachmentService.uploadBugAttachment(bugId, file, attachmentType, uploadedBy);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{bugId}/attachments")
    public ResponseEntity<List<AttachmentResponse>> getAttachments(
            @PathVariable Integer bugId) {

        List<AttachmentResponse> attachments = attachmentService.getAttachmentsByBugId(bugId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(
            @PathVariable Integer attachmentId) {

        Attachment attachment = attachmentService.getAttachmentFile(attachmentId);
        String contentType = attachment.getFileType() != null ? attachment.getFileType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
    }

    private final BugService bugService;
    @GetMapping("/{bugId}")
    public ResponseEntity<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> getById(
            @PathVariable Integer bugId) {

        return ResponseEntity.ok(
                bugService.getById(bugId));
    }

    @GetMapping
    public ResponseEntity<Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse>> getAllBugs(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        return ResponseEntity.ok(
                bugService.getAllBugs(page, size));
    }
    @GetMapping("/search")
    public ResponseEntity<Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse>> globalSearch(

            @RequestParam(required = false)
            String keyword,

            @RequestParam(required = false)
            BugSeverity severity,

            @RequestParam(required = false)
            BugPriority priority,

            @RequestParam(required = false)
            BugStatus status,

            @RequestParam(required = false)
            BugCategory category,

            @RequestParam(required = false)
            Integer bugOccurrence,

            @RequestParam(required = false)
            Boolean isActive,

            @RequestParam(required = false)
            LocalDate resolvedFrom,

            @RequestParam(required = false)
            String executedBy,

            @RequestParam(required = false)
            String assignedTo,

            @RequestParam(required = false)
            String updatedBy,

            @RequestParam(required = false)
            LocalDate resolvedTo,

            @RequestParam(required = false)
            String timeZone,

            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            @ParameterObject
            Pageable pageable
    ) {

        Page<xyz.mobi.testingautomationtool.dto.response.postMethodDTO.BugResponse> response =
                bugService.globalSearch(
                        keyword,
                        severity,
                        priority,
                        status,
                        category,
                        bugOccurrence,
                        isActive,
                        resolvedFrom,
                        resolvedTo,
                        timeZone,
                        pageable,
                        executedBy,
                        assignedTo,
                        updatedBy
                );

        return ResponseEntity.ok(response);
    }

}