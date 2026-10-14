package xyz.mobi.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.mobi.notificationservice.entity.Notification;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * DTO representing notification tracking details for history queries.
 * Excludes email body and attachment contents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Notification tracking details")
public class NotificationHistoryDto {

    @Schema(description = "Unique notification tracking identifier", example = "550e8400-e29b-41d4-a716-446655440000")
    private String notificationId;

    @Schema(description = "Sender email address", example = "notifications@company.com")
    private String sender;

    @Schema(description = "Primary recipients (TO)", example = "[\"user@example.com\"]")
    private List<String> to;

    @Schema(description = "CC recipients", example = "[\"manager@example.com\"]")
    private List<String> cc;

    @Schema(description = "BCC recipients", example = "[\"archive@example.com\"]")
    private List<String> bcc;

    @Schema(description = "Email subject line", example = "Order Confirmation #ORD-10928")
    private String subject;

    @Schema(description = "Submission lifecycle status (PENDING, SUBMITTED, FAILED)", example = "SUBMITTED")
    private String status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "Creation timestamp (UTC)")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Schema(description = "Submission timestamp to SMTP (UTC)")
    private Instant submittedAt;

    @Schema(description = "Concise reason for submission failure, if applicable", example = "SMTP server rejected recipient address")
    private String failureReason;

    public static NotificationHistoryDto fromEntity(Notification entity) {
        if (entity == null) {
            return null;
        }

        List<String> toList = entity.getRecipients() != null && !entity.getRecipients().isBlank()
                ? Arrays.stream(entity.getRecipients().split(",")).map(String::trim).toList()
                : List.of();

        List<String> ccList = entity.getCcRecipients() != null && !entity.getCcRecipients().isBlank()
                ? Arrays.stream(entity.getCcRecipients().split(",")).map(String::trim).toList()
                : null;

        List<String> bccList = entity.getBccRecipients() != null && !entity.getBccRecipients().isBlank()
                ? Arrays.stream(entity.getBccRecipients().split(",")).map(String::trim).toList()
                : null;

        return NotificationHistoryDto.builder()
                .notificationId(entity.getId())
                .sender(entity.getSender())
                .to(toList)
                .cc(ccList)
                .bcc(bccList)
                .subject(entity.getSubject())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .submittedAt(entity.getSubmittedAt())
                .failureReason(entity.getFailureReason())
                .build();
    }
}
