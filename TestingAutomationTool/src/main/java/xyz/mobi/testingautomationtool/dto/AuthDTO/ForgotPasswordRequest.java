package xyz.mobi.testingautomationtool.dto.AuthDTO;

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
@Schema(description = "Request payload to initiate password reset via email OTP")
public class ForgotPasswordRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    @Schema(description = "Registered user email address", example = "user@example.com")
    private String email;
}
