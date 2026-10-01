package xyz.mobi.testingautomationtool.dto.AuthDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Integer userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String designation;
    private String message;
}
