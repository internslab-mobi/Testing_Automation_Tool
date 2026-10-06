package xyz.mobi.testingautomationtool.dto.AuthDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request payload to reset password using verified email OTP")
public class ResetPasswordWithOtpRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered user email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP code is required")
    @Schema(description = "6-digit OTP code received via email", example = "481920")
    private String otpCode;

    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "New password must be at least 6 characters long")
    @Schema(description = "New user password", example = "NewSecurePass123!")
    private String newPassword;
}
