package xyz.mobi.testingautomationtool.dto.AuthDto;

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

    @Schema(description = "15-minute Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String accessToken;

    @Schema(description = "24-hour Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String refreshToken;

    @Schema(description = "Access Token lifespan in seconds (900s = 15 mins)", example = "900")
    private Long expiresIn;

    @Schema(description = "Refresh Token lifespan in seconds (86400s = 24 hrs)", example = "86400")
    private Long refreshTokenExpiresIn;

    @Builder.Default
    @Schema(description = "Token type", example = "Bearer")
    private String type = "Bearer";

    private Integer userId;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String designation;
    private String message;

    @Schema(description = "Session expiry alert (e.g., when refresh token has 10 minutes or less remaining)")
    private String sessionExpiryWarning;
}
