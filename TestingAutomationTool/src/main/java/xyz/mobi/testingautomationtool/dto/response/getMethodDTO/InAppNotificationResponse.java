package xyz.mobi.testingautomationtool.dto.response.getMethodDTO;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
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

