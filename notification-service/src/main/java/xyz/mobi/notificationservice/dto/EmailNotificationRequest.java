package xyz.mobi.notificationservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload for sending an email notification via Microsoft Outlook SMTP")
public class EmailNotificationRequest {


    @NotEmpty(message = "Recipient list 'to' must contain at least one recipient")
    @Schema(description = "List of primary recipient email addresses", example = "[\"developer@example.com\", \"tester@example.com\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<@NotBlank(message = "Recipient email cannot be blank") @Email(message = "Recipient email format is invalid") String> to;

    @Schema(description = "Optional list of CC (carbon copy) recipient email addresses", example = "[\"manager@example.com\"]")
    private List<@NotBlank(message = "CC recipient email cannot be blank") @Email(message = "CC recipient email format is invalid") String> cc;

    @Schema(description = "Optional list of BCC (blind carbon copy) recipient email addresses", example = "[\"audit@example.com\"]")
    private List<@NotBlank(message = "BCC recipient email cannot be blank") @Email(message = "BCC recipient email format is invalid") String> bcc;

    @NotBlank(message = "Email subject is required")
    @Size(max = 500, message = "Email subject must not exceed 500 characters")
    @Schema(description = "Subject line of the email", example = "Order Confirmation #ORD-10928", requiredMode = Schema.RequiredMode.REQUIRED)
    private String subject;

    @NotBlank(message = "Email body is required")
    @Schema(description = "Content of the email. Can be rich HTML or plain text depending on 'isHtml' flag.", example = "<h1>Order Confirmed</h1><p>Thank you for your order.</p>", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;

    @Builder.Default
    @Schema(description = "Specifies whether the body should be interpreted and rendered as HTML (true) or plain text (false). Default is false.", example = "true")
    private Boolean isHtml = false;

    @Email(message = "Reply-to email format is invalid")
    @Schema(description = "Optional reply-to email address", example = "no-reply@example.com")
    private String replyTo;

    @Schema(description = "Optional list of Base64-encoded attachments")
    private List<@Valid EmailAttachmentDto> attachments;

    @Schema(description = "Optional custom email headers (e.g., X-Priority, X-Entity-Ref-ID). Strictly validated to prevent CRLF injection.", example = "{\"X-Priority\": \"1\"}")
    private Map<String, String> headers;

    @Schema(description = "Optional application metadata for correlation and tracing (e.g., correlationId, externalReferenceId). Will not be logged with sensitive content.", example = "{\"correlationId\": \"corr-9988\", \"sourceSystem\": \"billing-app\"}")
    private Map<String, Object> metadata;
}
