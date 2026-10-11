package xyz.mobi.testingautomationtool.dto.NotificationDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InAppNotificationResponse {
    private Integer notificationId;
    private Integer bugId;
    private String bugFormatId;
    private String message;
    private String notificationStatus;
    private Instant createdAt;
}
