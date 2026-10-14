package xyz.mobi.notificationservice.exception;

/**
 * Thrown when a requested notification tracking record cannot be found.
 */
public class NotificationNotFoundException extends NotificationException {

    public NotificationNotFoundException(String notificationId) {
        super("NOTIFICATION_NOT_FOUND", "Notification record with ID '" + notificationId + "' not found.");
    }
}
