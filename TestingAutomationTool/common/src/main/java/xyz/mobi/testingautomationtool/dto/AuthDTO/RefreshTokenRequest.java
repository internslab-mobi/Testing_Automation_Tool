package xyz.mobi.testingautomationtool.dto.AuthDTO;

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
@Schema(description = "Refresh Token Request payload")
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token cannot be blank")
    @Schema(description = "24-hour Refresh Token received upon login", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String refreshToken;
}
