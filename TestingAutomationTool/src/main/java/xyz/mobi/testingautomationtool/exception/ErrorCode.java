package xyz.mobi.testingautomationtool.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "The request is invalid", "TA-400"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Request validation failed", "TA-401"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found", "TA-404"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "The resource already exists", "TA-409"),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "The request violates a business rule", "TA-422"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "The request conflicts with existing data", "TA-423"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "TA-500");

    private final HttpStatus responseCode;
    private final String responseMessage;
    private final String internalErrorCode;

    ErrorCode(HttpStatus responseCode, String responseMessage, String internalErrorCode) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.internalErrorCode = internalErrorCode;
    }
}
