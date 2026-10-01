package xyz.mobi.testingautomationtool.dto.NotificationDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private Integer employeeId;
    private Integer assignedId;
    private Integer bugId;
}
