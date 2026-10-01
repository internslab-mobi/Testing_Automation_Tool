package xyz.mobi.testingautomationtool.dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.enums.InAppNotificationStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPatchRequest {
    private InAppNotificationStatus status;
}
