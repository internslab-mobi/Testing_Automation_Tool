package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.client.NotificationServiceClient;
import xyz.mobi.testingautomationtool.dto.notification.EmailNotificationRequest;
import xyz.mobi.testingautomationtool.service.EmailService;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final NotificationServiceClient notificationServiceClient;

    @Async
    @Override
    public void sendPasswordResetOtpEmail(String toEmail, String username, String otpCode, int expiryMinutes) {
        if (toEmail == null || toEmail.isBlank()) {
            return;
        }

        String subject = "Your Password Reset OTP - Testing Automation Tool";

        String message = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f8fafc; padding: 20px;">
                    <div style="max-width: 600px; margin: 0 auto; background: #ffffff; border-radius: 8px; padding: 30px; border: 1px solid #e2e8f0;">
                        <h2 style="color: #1e293b; margin-top: 0;">Password Reset Request</h2>
                        <p style="color: #475569; font-size: 15px;">Hello <strong>%s</strong>,</p>
                        <p style="color: #475569; font-size: 14px; line-height: 1.6;">
                            We received a request to reset your password. Use the OTP code below to proceed:
                        </p>
                        <div style="background: #eef2ff; border: 2px dashed #6366f1; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #4338ca; font-family: monospace;">%s</span>
                            <p style="margin: 10px 0 0 0; font-size: 13px; color: #64748b;">Valid for %d minutes only.</p>
                        </div>
                        <p style="color: #dc2626; font-size: 13px;">If you did not request this, please ignore this email.</p>
                    </div>
                </body>
                </html>
                """.formatted(escapeHtml(username), escapeHtml(otpCode), expiryMinutes);

        sendHtmlEmail(toEmail, subject, message);
    }

    private void sendHtmlEmail(String recipientEmail, String subject, String content) {
        EmailNotificationRequest request = EmailNotificationRequest.builder()
                .to(List.of(recipientEmail))
                .subject(subject)
                .body(content)
                .isHtml(true)
                .metadata(Map.of("sourceSystem", "auth-service", "purpose", "PASSWORD_RESET_OTP"))
                .build();

        notificationServiceClient.sendEmail(request);
    }

    private String escapeHtml(String value) {
        if (value == null) return "N/A";
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
