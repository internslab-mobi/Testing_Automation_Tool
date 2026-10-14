package xyz.mobi.notificationservice.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.notificationservice.dto.ApiResponse;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiResponse<Void>> buildErrorResponse(
            String errorCode,
            String errorMessage,
            HttpStatus status) {

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(errorMessage)
                .errorStatusCode(status.value())
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(response, status);
    }

    // 1. Validation Failures (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation failed for request: {}", errors);

        ApiResponse<Void> response = ApiResponse.validationError(
                "VALIDATION_FAILED",
                "Request validation failed. Please correct the invalid fields.",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return buildErrorResponse("CONSTRAINT_VIOLATION", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed JSON request body: {}", ex.getMessage());
        return buildErrorResponse("MALFORMED_REQUEST", "Malformed JSON request body or unparseable field format.", HttpStatus.BAD_REQUEST);
    }

    // 2. Custom Business Invalid Request (400)
    @ExceptionHandler(InvalidEmailRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidEmailRequest(InvalidEmailRequestException ex) {
        log.warn("Invalid email request: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 3. Payload Too Large (413)
    @ExceptionHandler(AttachmentSizeLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttachmentSizeLimitExceeded(AttachmentSizeLimitExceededException ex) {
        log.warn("Attachment size limit exceeded: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // 4. Unsupported Media Type (415)
    @ExceptionHandler(UnsupportedAttachmentTypeException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedAttachmentType(UnsupportedAttachmentTypeException ex) {
        log.warn("Unsupported attachment type: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("Unsupported Content-Type header: {}", ex.getMessage());
        return buildErrorResponse("UNSUPPORTED_MEDIA_TYPE", ex.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    // 5. SMTP Email Delivery Failure (502)
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailDelivery(EmailDeliveryException ex) {
        log.error("Email delivery failed: errorCode={}, message={}", ex.getErrorCode(), ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.BAD_GATEWAY);
    }

    // 6. Notification Not Found (404)
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotificationNotFound(NotificationNotFoundException ex) {
        log.warn("Notification not found: {}", ex.getMessage());
        return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 7. Database Persistence / Connectivity Failure (500)
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataAccessException(org.springframework.dao.DataAccessException ex) {
        log.error("Database persistence failure: {}", ex.getMessage(), ex);
        return buildErrorResponse("DATABASE_ERROR", "Database operation failed. Verify database connectivity, permissions, and schema configuration.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 8. General Catch-All (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception in notification service", ex);
        return buildErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred while processing the notification.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
