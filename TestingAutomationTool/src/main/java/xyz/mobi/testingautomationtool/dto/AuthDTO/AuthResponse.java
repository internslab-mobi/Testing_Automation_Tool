package xyz.mobi.testingautomationtool.dto.AuthDTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private Integer userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
}
