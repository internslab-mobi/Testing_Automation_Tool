package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(Integer bugId, CommentRequest request);
    List<CommentResponse> getCommentsByBugId(Integer bugId);
}
