package xyz.mobi.notificationservice.exception;

public class UnsupportedAttachmentTypeException extends NotificationException {

    public UnsupportedAttachmentTypeException(String message) {
        super("UNSUPPORTED_ATTACHMENT_TYPE", message);
    }
}
