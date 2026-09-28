package xyz.mobi.testingautomationtool.dto.request.postMethodDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {

    private Integer employeeId;

    private Integer assignedId;

    private Integer bugId;

}
