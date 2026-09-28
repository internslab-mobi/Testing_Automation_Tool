package xyz.mobi.testingautomationtool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.CommentPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.CommentResponse;
import xyz.mobi.testingautomationtool.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/bugs/{bugId}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable("bugId") Integer bugId,
            @Valid @RequestBody CommentRequest request) {

        CommentResponse response = commentService.addComment(bugId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/bugs/{bugId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable("bugId") Integer bugId) {

        List<CommentResponse> response = commentService.getCommentsByBugId(bugId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bugs/{bugId}/comments/page")
    public ResponseEntity<Page<CommentResponse>> getCommentsPaged(
            @PathVariable("bugId") Integer bugId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        Page<CommentResponse> response = commentService.getCommentsByBugId(bugId, pageable);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable("commentId") Integer commentId,
            @Valid @RequestBody CommentPutRequest request) {

        CommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable("commentId") Integer commentId) {

        String response = commentService.deleteComment(commentId);
        return ResponseEntity.ok(response);
    }
}
