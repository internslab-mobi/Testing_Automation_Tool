package xyz.mobi.testingautomationtool.exception;

/**
 * Parent business exception for all custom application exceptions.
 */
public class TestingAutomationException extends RuntimeException {

    public TestingAutomationException() {
        super();
    }

    public TestingAutomationException(String message) {
        super(message);
    }

    public TestingAutomationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestingAutomationException(Throwable cause) {
        super(cause);
    }
}
