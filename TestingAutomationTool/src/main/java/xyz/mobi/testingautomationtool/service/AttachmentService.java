package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

import java.io.IOException;
import java.util.List;

public interface AttachmentService {

    List<AttachmentResponse> uploadAttachments(Integer bugId, List<MultipartFile> file, AttachmentType attachmentType) throws IOException;

    List<AttachmentResponse> getAttachmentsByBugId(Integer bugId);


}
