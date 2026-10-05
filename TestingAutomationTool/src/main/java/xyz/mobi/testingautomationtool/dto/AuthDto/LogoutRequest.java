package xyz.mobi.testingautomationtool.dto.AuthDto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to revoke refresh token and logout")
public class LogoutRequest {

    @NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh Token to be revoked upon logout")
    private String refreshToken;
}
