package xyz.mobi.testingautomationtool.dto.response.managerResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.testingautomationtool.entity.Role;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class MangerGetResponseOfUserEntity {
    Integer userId;
    String username;
    String fullName;
    String email;
    Role role;
}
