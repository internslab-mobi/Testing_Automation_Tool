package xyz.mobi.notificationservice.exception;

public class EmailDeliveryException extends NotificationException {

    public EmailDeliveryException(String message) {
        super("EMAIL_SEND_FAILED", message);
    }

    public EmailDeliveryException(String errorCode, String message) {
        super(errorCode, message);
    }

    public EmailDeliveryException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
