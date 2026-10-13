package xyz.mobi.testingautomationtool.dto.AuthDTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to verify the 6-digit email OTP")
public class VerifyOtpRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered user email address", example = "user@example.com")
    private String email;

    @NotBlank(message = "OTP code is required")
    @JsonAlias({"otp", "otpCode"})
    @Schema(description = "6-digit OTP code received via email", example = "481920")
    private String otpCode;
}
