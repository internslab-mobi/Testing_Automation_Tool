package xyz.mobi.testingautomationtool.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationRequest {

    private List<String> to;

    private List<String> cc;

    private List<String> bcc;

    private String subject;

    private String body;

    @Builder.Default
    private Boolean isHtml = true;

    private String replyTo;

    private List<EmailAttachmentDto> attachments;

    private Map<String, String> headers;

    private Map<String, Object> metadata;
}
