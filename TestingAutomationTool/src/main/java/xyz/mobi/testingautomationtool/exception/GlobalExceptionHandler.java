package xyz.mobi.testingautomationtool.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.testingautomationtool.dto.response.ErrorResponse;
import xyz.mobi.testingautomationtool.enums.ErrorCode;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String errorCode,
            String errorMessage,
            HttpStatus status) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .errorStatusCode(status.value())
                .time(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(NullPointerException ex) {
        log.error("NullPointerException occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : ErrorCode.NULL_POINTER_EXCEPTION.getDefaultMessage();
        return buildErrorResponse(ErrorCode.NULL_POINTER_EXCEPTION.getCode(), message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.RESOURCE_NOT_FOUND.getCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_ARGUMENT.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        log.warn("IllegalStateException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.ILLEGAL_STATE.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Validation failed: {}", errorMessage);
        return buildErrorResponse(ErrorCode.VALIDATION_FAILED.getCode(), errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: ", ex);
        String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        return buildErrorResponse(ErrorCode.DATABASE_ERROR.getCode(), message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException ex) {
        log.warn("GlobalException: {}", ex.getMessage());
        return buildErrorResponse(ErrorCode.INVALID_ARGUMENT.getCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(java.lang.Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(java.lang.Exception ex) {
        log.error("Unhandled exception occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage();
        return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
