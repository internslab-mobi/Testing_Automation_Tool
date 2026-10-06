package xyz.mobi.testingautomationtool.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentPutRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Integer bugId, CommentRequest request);

    List<CommentResponse> getCommentsByBugId(Integer bugId);

    Page<CommentResponse> getCommentsByBugId(Integer bugId, Pageable pageable);

    CommentResponse updateComment(Integer commentId, CommentPutRequest request);

    String deleteComment(Integer commentId);
}
