package xyz.mobi.notificationservice.exception;

public class InvalidEmailRequestException extends NotificationException {

    public InvalidEmailRequestException(String message) {
        super("INVALID_EMAIL_REQUEST", message);
    }

    public InvalidEmailRequestException(String errorCode, String message) {
        super(errorCode, message);
    }
}
