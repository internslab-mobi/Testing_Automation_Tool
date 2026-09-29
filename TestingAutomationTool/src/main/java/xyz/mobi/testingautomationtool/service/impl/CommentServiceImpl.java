package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.request.postMethodDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.request.putMethodDTO.CommentPutRequest;
import xyz.mobi.testingautomationtool.dto.response.postMethodDTO.CommentResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Comment;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.ResourceNotFoundException;
import xyz.mobi.testingautomationtool.mapper.getMapper.CommentMapper;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.CommentRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.CommentService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentResponse addComment(Integer bugId, CommentRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new ResourceNotFoundException("Bug not found with ID: " + bugId));

        if (bug.isDeleted() || !bug.isActive()) {
            throw new IllegalStateException("Cannot comment on a deleted or inactive bug with ID: " + bugId);
        }

        Integer userId = request.getUserId() != null ? request.getUserId() : 1;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Comment comment = Comment.builder()
                .bug(bug)
                .comment(request.getComment())
                .createdBy(user)
                .createdAt(Instant.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        //inAppNotificationService.createNewCommentNotification(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBugId(Integer bugId) {
        if (!bugRepository.existsById(bugId)) {
            throw new ResourceNotFoundException("Bug not found with ID: " + bugId);
        }

        List<Comment> comments = commentRepository.findByBug_BugIdOrderByCreatedAtAsc(bugId);
        return comments.stream()
                .map(commentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByBugId(Integer bugId, Pageable pageable) {
        if (!bugRepository.existsById(bugId)) {
            throw new ResourceNotFoundException("Bug not found with ID: " + bugId);
        }

        Page<Comment> comments = commentRepository.findByBug_BugIdOrderByCreatedAtAsc(bugId, pageable);
        return comments.map(commentMapper::toResponse);
    }

    @Override
    public CommentResponse updateComment(Integer commentId, CommentPutRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        comment.setComment(request.getComment());
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toResponse(savedComment);
    }

    @Override
    public String deleteComment(Integer commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with ID: " + commentId));

        commentRepository.delete(comment);
        return "Comment with ID " + commentId + " deleted successfully";
    }
}
