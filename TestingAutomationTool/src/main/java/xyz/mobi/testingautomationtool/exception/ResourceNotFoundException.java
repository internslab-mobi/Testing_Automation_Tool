package xyz.mobi.testingautomationtool.exception;

public class ResourceNotFoundException extends TestingAutomationException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
