package xyz.mobi.testingautomationtool.exception;

public class GlobalException extends TestingAutomationException {

    public GlobalException(String message) {
        super(message);
    }

    public GlobalException(String message, Throwable cause) {
        super(message, cause);
    }
}
