package xyz.mobi.testingautomationtool.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.testingautomationtool.dto.response.ErrorResponse;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final ErrorDataRepository errorDataRepository;
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
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.error("NullPointerException occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() :"Internal Server Error";
        return buildErrorResponse(mapping, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(mapping, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return buildErrorResponse(mapping, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.warn("IllegalStateException: {}", ex.getMessage());
        return buildErrorResponse(mapping, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.warn("Validation failed: {}", errorMessage);
        return buildErrorResponse(mapping, errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: ", ex);
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        return buildErrorResponse(mapping, message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.warn("GlobalException: {}", ex.getMessage());
        return buildErrorResponse(mapping, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(java.lang.Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(java.lang.Exception ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);
        log.error("Unhandled exception occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Internal Server Error";
        return buildErrorResponse(mapping, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private String getErrorCode(String exceptionName){
        Error mapping = errorDataRepository.findByExceptionName(exceptionName)
                .orElseThrow();
        return mapping.getErrorCode();
    }
}
