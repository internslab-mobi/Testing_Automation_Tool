package xyz.mobi.testingautomationtool.exception;

public class EmailNotFoundException extends TestingAutomationException {

    public EmailNotFoundException(String message) {
        super(message);
    }

    public EmailNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
