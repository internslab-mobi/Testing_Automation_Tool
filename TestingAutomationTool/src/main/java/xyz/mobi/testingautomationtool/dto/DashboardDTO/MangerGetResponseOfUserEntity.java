package xyz.mobi.testingautomationtool.dto.DashboardDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.entity.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MangerGetResponseOfUserEntity {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private Role role;
}
