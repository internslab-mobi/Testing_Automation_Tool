package xyz.mobi.testingautomationtool.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorDataRepository errorDataRepository;


    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<ErrorResponse> handleExcelValidationException(
            ExcelValidationException ex) {

        log.warn("Excel validation failed: {}", ex.getMessage());

        return buildErrorResponse(
                ex,
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<ErrorResponse> handleExcelProcessingException(
            ExcelProcessingException ex) {

        log.error("Excel processing failed", ex);

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return buildErrorResponse(
                ex,
                HttpStatus.NOT_FOUND
        );
    }


    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException ex) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return buildErrorResponse(
                ex,
                HttpStatus.CONFLICT
        );
    }


    // =========================================================
    // COMMON ILLEGAL ARGUMENT
    // =========================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        log.warn("Invalid argument: {}", ex.getMessage());

        return buildErrorResponse(
                ex,
                HttpStatus.BAD_REQUEST
        );
    }


    // =========================================================
    // COMMON RUNTIME EXCEPTION
    // =========================================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex) {

        log.error("Unexpected runtime exception", ex);

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    // =========================================================
    // COMMON CHECKED / GENERAL EXCEPTION
    // =========================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex) {

        log.error("Unexpected exception", ex);

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        log.warn("Request validation failed: {}", ex.getMessage());

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        return buildErrorResponse(
                ex,
                errorMessage,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        log.warn("Invalid request body: {}", ex.getMessage());

        return buildErrorResponse(
                ex,
                "Invalid request data",
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AttachmentProcessingException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentProcessingException(
            AttachmentProcessingException ex) {

        log.error("Attachment processing failed", ex);

        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    // =========================================================
    // COMMON RESPONSE BUILDER
    // =========================================================
    private ResponseEntity<ErrorResponse> buildErrorResponse(
            java.lang.Exception exception,
            HttpStatus status) {

        String errorCode = getErrorCode(exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .errorStatus(status.value())
                .errorMessage(exception.getMessage())
                .errorTime(Instant.now())
                .build();

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            java.lang.Exception exception,
            String errorMessage,
            HttpStatus status) {

        String errorCode = getErrorCode(exception);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .errorStatus(status.value())
                .errorMessage(errorMessage)
                .errorTime(Instant.now())
                .build();

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }




    // =========================================================
    // GET ERROR CODE FROM DATABASE
    // =========================================================

    private String getErrorCode(Exception exception) {

        String exceptionName =
                exception.getClass().getSimpleName();

        return errorDataRepository
                .findByExceptionName(exceptionName)
                .map(Error::getErrorCode)
                .orElse("ERR-999");
    }
}