package xyz.mobi.testingautomationtool.dto.AdminDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManagerResponse {

    private Integer userId;
    private String username;
    private String fullName;
    private String designation;
    private String skills;
    private String email;
    private String role;
}
