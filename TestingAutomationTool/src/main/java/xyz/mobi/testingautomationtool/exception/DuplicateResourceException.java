package xyz.mobi.testingautomationtool.exception;

public class DuplicateResourceException extends TestingAutomationException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
