package xyz.mobi.testingautomationtool.exception;

public class InvalidTokenException extends TestingAutomationException {

    public InvalidTokenException(String message) {
        super(message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidTokenException(Throwable cause) {
        super(cause);
    }
}
