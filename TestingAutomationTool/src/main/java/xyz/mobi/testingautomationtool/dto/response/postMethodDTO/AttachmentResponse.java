package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Integer attachmentId;

    private String fileName;

    private String contentType;

    private Long fileSize;

    private Integer projectId;

    private Instant createdAt;
}
