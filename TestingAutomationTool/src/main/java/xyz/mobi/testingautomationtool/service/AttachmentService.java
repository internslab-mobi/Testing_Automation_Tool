package xyz.mobi.testingautomationtool.service;

import org.springframework.web.multipart.MultipartFile;

import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentDownloadResponse;
import xyz.mobi.testingautomationtool.dto.AttachmentDTO.AttachmentResponse;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

import java.io.IOException;
import java.util.List;

//public interface AttachmentService {
//
//    List<AttachmentResponse> uploadAttachments(AttachmentType type, Integer entityId, List<MultipartFile> files);
//
//    List<AttachmentResponse> getAttachments(AttachmentType type, Integer entityId);
//
//    AttachmentDownloadResponse downloadAttachments(AttachmentType type, Integer entityId);
//
//    String deleteAttachment(Integer attachmentId);
//}


public interface AttachmentService {

    // Common attachment logic
    List<AttachmentResponse> uploadAttachments(
            AttachmentType type,
            Integer entityId,
            List<MultipartFile> files);

    // Entity-specific methods
    List<AttachmentResponse> uploadBugAttachments(
            Integer bugId,
            List<MultipartFile> files);

    List<AttachmentResponse> uploadFeatureAttachments(
            Integer featureId,
            List<MultipartFile> files);

    List<AttachmentResponse> uploadProjectAttachments(
            Integer projectId,
            List<MultipartFile> files);

    List<AttachmentResponse> uploadTestCaseAttachments(
            Integer testcaseId,
            List<MultipartFile> files);

    List<AttachmentResponse> getAttachments(
            AttachmentType type,
            Integer entityId);

    AttachmentDownloadResponse downloadAttachments(
            AttachmentType type,
            Integer entityId);

    String deleteAttachment(Integer attachmentId);
}