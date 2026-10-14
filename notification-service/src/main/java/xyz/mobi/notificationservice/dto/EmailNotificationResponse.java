package xyz.mobi.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response payload after successful SMTP submission")
public class EmailNotificationResponse {

    @Schema(description = "Unique identifier generated for this notification request", example = "550e8400-e29b-41d4-a716-446655440000")
    private String notificationId;

    @Builder.Default
    @Schema(description = "Status of the email dispatch", example = "SUBMITTED")
    private String status = "SUBMITTED";

    @Schema(description = "Total number of targeted recipients (to + cc + bcc)", example = "3")
    private int recipientCount;

    @Schema(description = "Total number of successfully processed attachments", example = "1")
    private int attachmentCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Builder.Default
    @Schema(description = "UTC timestamp when the message was submitted to the SMTP server")
    private Instant submittedAt = Instant.now();

    @Schema(description = "Echo of correlation metadata passed in the request", example = "{\"correlationId\": \"corr-9988\"}")
    private Map<String, Object> metadata;
}
