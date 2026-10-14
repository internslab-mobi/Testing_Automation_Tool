package xyz.mobi.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO representing an email attachment encoded in Base64")
public class EmailAttachmentDto {

    @NotBlank(message = "Attachment fileName is required")
    @Size(max = 255, message = "Attachment fileName must not exceed 255 characters")
    @Pattern(regexp = "^[^/\\\\:\\*\\?\"<>\\|]+$", message = "Attachment fileName contains illegal characters or path separators")
    @Schema(description = "Filename including extension, e.g., 'invoice_1024.pdf'", example = "invoice_1024.pdf")
    private String fileName;

    @Schema(description = "MIME content type, e.g., 'application/pdf', 'text/html', 'image/png'", example = "application/pdf")
    private String contentType;

    @NotBlank(message = "Attachment contentBase64 is required")
    @Schema(description = "Base64 encoded binary content of the attachment", example = "SGVsbG8gV29ybGQ=")
    private String contentBase64;
}
