package xyz.mobi.testingautomationtool.dto.response.postMethodDTO;

import lombok.*;
import xyz.mobi.testingautomationtool.enums.NotificationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Integer notificationId;

    private String employeeName;

    private String message;

    private Integer bugId;

    private String bugFormatId;

    private LocalDateTime createdAt;

    private NotificationStatus notificationStatus;
}