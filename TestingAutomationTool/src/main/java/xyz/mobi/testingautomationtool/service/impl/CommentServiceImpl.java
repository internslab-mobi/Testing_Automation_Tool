package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentRequest;
import xyz.mobi.testingautomationtool.dto.CommentDTO.CommentResponse;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.Comment;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.CommentRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.CommentService;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CommentResponse addComment(Integer bugId, CommentRequest request) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (bug.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        User user = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        Comment comment = Comment.builder()
                .bug(bug)
                .comment(request.getComment())
                .createdBy(user)
                .createdAt(Instant.now())
                .build();

        Comment saved = commentRepository.save(comment);

        return CommentResponse.builder()
                .commentId(saved.getCommentId())
                .bugId(saved.getBug().getBugId())
                .comment(saved.getComment())
                .createdBy(saved.getCreatedBy().getUserId())
                .creatorName(saved.getCreatedBy().getFullName() != null ? saved.getCreatedBy().getFullName() : saved.getCreatedBy().getUsername())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByBugId(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (bug.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return commentRepository.findByBug_BugIdOrderByCreatedAtAsc(bugId).stream()
                .map(c -> CommentResponse.builder()
                        .commentId(c.getCommentId())
                        .bugId(c.getBug().getBugId())
                        .comment(c.getComment())
                        .createdBy(c.getCreatedBy().getUserId())
                        .creatorName(c.getCreatedBy().getFullName() != null ? c.getCreatedBy().getFullName() : c.getCreatedBy().getUsername())
                        .createdAt(c.getCreatedAt())
                        .build())
                .toList();
    }
}
