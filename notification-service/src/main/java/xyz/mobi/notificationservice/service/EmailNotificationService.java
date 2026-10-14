package xyz.mobi.notificationservice.service;

import org.springframework.data.domain.Pageable;
import xyz.mobi.notificationservice.dto.EmailNotificationRequest;
import xyz.mobi.notificationservice.dto.EmailNotificationResponse;
import xyz.mobi.notificationservice.dto.NotificationHistoryDto;
import xyz.mobi.notificationservice.dto.PaginatedResponse;

public interface EmailNotificationService {

    /**
     * Dispatches an email notification through Microsoft Outlook SMTP and persists tracking information.
     *
     * @param request the validated email notification request
     * @return submission response details including generated notificationId
     */
    EmailNotificationResponse sendEmail(EmailNotificationRequest request);

    /**
     * Retrieves tracking details for a specific notification by its ID.
     *
     * @param notificationId unique identifier of the notification
     * @return tracking history details
     */
    NotificationHistoryDto getNotificationById(Long notificationId);

    /**
     * Retrieves paginated notification tracking records.
     *
     * @param pageable pagination parameters (page, size, sort)
     * @return paginated notification history
     */
    PaginatedResponse<NotificationHistoryDto> getNotificationHistory(Pageable pageable);
}
