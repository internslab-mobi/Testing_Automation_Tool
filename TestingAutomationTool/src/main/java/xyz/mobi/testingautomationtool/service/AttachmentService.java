package xyz.mobi.testingautomationtool.service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDto.AttachmentResponse;
import xyz.mobi.testingautomationtool.entity.Attachment;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

import java.util.List;

public interface AttachmentService {
    AttachmentResponse uploadBugAttachment(Integer bugId, MultipartFile file, AttachmentType attachmentType, Integer uploadedBy);
    List<AttachmentResponse> getAttachmentsByBugId(Integer bugId);
}
