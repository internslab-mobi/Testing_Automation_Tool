package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.entity.Bug;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.enums.AttachmentType;
import xyz.mobi.testingautomationtool.exception.CustomException;
import xyz.mobi.testingautomationtool.exception.ErrorCode;
import xyz.mobi.testingautomationtool.repository.AttachmentRepository;
import xyz.mobi.testingautomationtool.repository.BugRepository;
import xyz.mobi.testingautomationtool.repository.UserRepository;
import xyz.mobi.testingautomationtool.service.AttachmentService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final BugRepository bugRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AttachmentResponse uploadBugAttachment(
            Integer bugId,
            MultipartFile file,
            AttachmentType attachmentType,
            Integer uploadedBy) {

        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }

        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (bug.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        User user = userRepository.findById(uploadedBy)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read file bytes: {}", e.getMessage());
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            fileName = "attachment_" + System.currentTimeMillis();
        }

        Attachment attachment = Attachment.builder()
                .bug(bug)
                .fileName(fileName)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .fileBlob(bytes)
                .attachmentType(attachmentType != null ? attachmentType : AttachmentType.BUG)
                .uploadedBy(user)
                .updatedBy(user)
                .isActive(true)
                .isDeleted(false)
                .build();

        Attachment saved = attachmentRepository.save(attachment);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsByBugId(Integer bugId) {
        Bug bug = bugRepository.findById(bugId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        if (bug.isDeleted()) {
            throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
        }

        return attachmentRepository.findByBug_BugIdAndIsDeletedFalse(bugId).stream()
                .map(this::toResponse)
                .toList();
    }


    private AttachmentResponse toResponse(Attachment a) {
        return AttachmentResponse.builder()
                .attachmentId(a.getAttachmentId())
                .bugId(a.getBug() != null ? a.getBug().getBugId() : null)
                .fileName(a.getFileName())
                .fileType(a.getFileType())
                .fileSize(a.getFileSize())
                .uploadedBy(a.getUploadedBy() != null ? a.getUploadedBy().getUserId() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}
