package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.time.Instant;

@Getter
@Setter
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