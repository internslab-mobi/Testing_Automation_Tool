package xyz.mobi.testingautomationtool.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import xyz.mobi.testingautomationtool.dto.notification.EmailNotificationRequest;
import xyz.mobi.testingautomationtool.exception.EmailSendingException;

@Slf4j
@Component
public class NotificationServiceClient {

    private final RestClient restClient;

    public NotificationServiceClient(
            @Value("${notification.service.url:http://localhost:8091}") String serviceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serviceUrl)
                .build();
        log.info("Initialized NotificationServiceClient targeting: {}", serviceUrl);
    }

    public void sendEmail(EmailNotificationRequest request) {
        if (request == null || request.getTo() == null || request.getTo().isEmpty()) {
            log.warn("Skipping email dispatch: Request or recipient list is empty");
            return;
        }

        try {
            restClient.post()
                    .uri("/api/v1/notifications/email")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email dispatched successfully via notification-service to recipients: {}", request.getTo());
        } catch (Exception ex) {
            log.error("Failed to send email via notification-service to {}: {}", request.getTo(), ex.getMessage());
            throw new EmailSendingException("Failed to dispatch email via notification service: " + ex.getMessage(), ex);
        }
    }
}
