package xyz.mobi.testingautomationtool.dto.AuthDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to reset password using the temporary reset token issued after OTP verification")
public class ResetPasswordRequest {

    @NotBlank(message = "Reset token is required")
    @JsonAlias({"token", "resetToken", "tokenCode"})
    @Schema(description = "Temporary reset token received after OTP verification", example = "550e8400-e29b-41d4-a716-446655440000")
    private String resetToken;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    @Schema(description = "New user password", example = "NewSecurePass123!")
    private String newPassword;

}
