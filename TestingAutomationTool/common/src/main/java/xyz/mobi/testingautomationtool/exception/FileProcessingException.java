package xyz.mobi.testingautomationtool.exception;

public class FileProcessingException extends TestingAutomationException {

    public FileProcessingException(String message) {
        super(message);
    }

    public FileProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
