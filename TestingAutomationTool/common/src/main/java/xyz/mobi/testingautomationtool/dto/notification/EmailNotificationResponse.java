package xyz.mobi.testingautomationtool.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationResponse {

    private String notificationId;
    private String status;
    private int recipientCount;
    private int attachmentCount;
    private Instant submittedAt;
    private Map<String, Object> metadata;
}
