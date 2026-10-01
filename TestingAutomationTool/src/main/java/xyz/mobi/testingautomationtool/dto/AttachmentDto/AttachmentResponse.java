package xyz.mobi.testingautomationtool.dto.AttachmentDto;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.AttachmentType;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private Integer attachmentId;
    private Integer bugId;
    private Integer testcaseId;
    private Integer featureId;
    private Integer projectId;
    private String fileName;
    private String fileType;
    private String contentType;
    private Long fileSize;
    private AttachmentType attachmentType;
    private Integer uploadedBy;
    private String uploaderName;
    private Instant createdAt;
}
