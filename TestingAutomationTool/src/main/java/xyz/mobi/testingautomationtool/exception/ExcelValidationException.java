package xyz.mobi.testingautomationtool.exception;

import xyz.mobi.testingautomationtool.dto.ExcelDTO.ExcelUploadErrorResponse;

import java.util.List;

public class ExcelValidationException extends TestingAutomationException {

    private final List<ExcelUploadErrorResponse> errors;

    public ExcelValidationException(String message) {
        super(message);
        this.errors = null;
    }

    public ExcelValidationException(String message, List<ExcelUploadErrorResponse> errors) {
        super(message);
        this.errors = errors;
    }

    public ExcelValidationException(String message, Throwable cause) {
        super(message, cause);
        this.errors = null;
    }

    public List<ExcelUploadErrorResponse> getErrors() {
        return errors;
    }
}