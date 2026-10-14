package xyz.mobi.notificationservice.exception;

public class AttachmentSizeLimitExceededException extends NotificationException {

    public AttachmentSizeLimitExceededException(String message) {
        super("ATTACHMENT_SIZE_EXCEEDED", message);
    }
}
