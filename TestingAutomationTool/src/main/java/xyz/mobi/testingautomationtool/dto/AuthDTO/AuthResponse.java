package xyz.mobi.testingautomationtool.dto.AuthDTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication Response containing Access and Refresh tokens")
public class AuthResponse {

    @Schema(description = "15-minute Access Token (alias for backward compatibility)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String token;

    @Schema(description = "24-hour Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String refreshToken;

    @Schema(description = "Access Token lifespan in seconds (900s = 15 mins)", example = "900")
    private Long expiresIn;

    @Schema(description = "Refresh Token lifespan in seconds (86400s = 24 hrs)", example = "86400")
    private Long refreshTokenExpiresIn;


    private Integer userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String designation;
    private String message;

    @Schema(description = "Temporary reset token obtained after verifying OTP, valid for resetting password", example = "550e8400-e29b-41d4-a716-446655440000")
    private String resetToken;

    @Schema(description = "Reset Token lifespan in seconds (900s = 15 mins)", example = "900")
    private Long resetTokenExpiresIn;

    @Schema(description = "Session expiry alert (e.g., when refresh token has 10 minutes or less remaining)")
    private String sessionExpiryWarning;
}
