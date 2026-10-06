package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;

import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    List<AttachmentResponse> uploadAttachments(AttachmentType type, Integer entityId, List<MultipartFile> files);

    List<AttachmentResponse> getAttachments(AttachmentType type, Integer entityId);

    AttachmentDownloadResponse downloadAttachments(AttachmentType type, Integer entityId);

    String deleteAttachment(Integer attachmentId);
}
