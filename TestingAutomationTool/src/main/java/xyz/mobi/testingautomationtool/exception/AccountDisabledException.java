package xyz.mobi.testingautomationtool.exception;

public class AccountDisabledException extends TestingAutomationException {

    public AccountDisabledException(String message) {
        super(message);
    }

    public AccountDisabledException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountDisabledException(Throwable cause) {
        super(cause);
    }
}
