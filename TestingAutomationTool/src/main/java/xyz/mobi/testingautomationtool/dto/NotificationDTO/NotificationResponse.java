package xyz.mobi.testingautomationtool.dto.NotificationDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Integer notificationId;
    private Integer employeeId;
    private Integer assignedId;
    private Integer bugId;
    private NotificationStatus notificationStatus;
    private Instant createdAt;
}
