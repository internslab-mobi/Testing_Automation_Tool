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

    @Schema(description = "24-hour Refresh Token (optional if provided via HttpOnly cookie)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String refreshToken;
}
