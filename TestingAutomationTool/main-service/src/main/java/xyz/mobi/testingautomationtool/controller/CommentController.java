package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentPutRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;
import xyz.mobi.testingautomationtool.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('MANAGER', 'TESTER', 'ADMIN')")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/bugs/{bugId}/comments")
    public ResponseEntity<ApiResponse<CommentResponse>> addComment(
            @PathVariable("bugId") Integer bugId,
            @Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.addComment(bugId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment added successfully", response));
    }

    @GetMapping("/bugs/{bugId}/comments")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
            @PathVariable("bugId") Integer bugId) {
        List<CommentResponse> response = commentService.getCommentsByBugId(bugId);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    @GetMapping("/bugs/{bugId}/comments/page")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsPaged(
            @PathVariable("bugId") Integer bugId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CommentResponse> response = commentService.getCommentsByBugId(bugId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Comments retrieved successfully", response));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable("commentId") Integer commentId,
            @Valid @RequestBody CommentPutRequest request) {
        CommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", response));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable("commentId") Integer commentId) {
        String response = commentService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
