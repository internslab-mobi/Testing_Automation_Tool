package xyz.mobi.testingautomationtool.exception;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorDataRepository errorDataRepository;

    private final Cache<String, Error> exceptionCache =
            Caffeine.newBuilder()
                    .maximumSize(100)
                    .expireAfterWrite(8, TimeUnit.HOURS)
                    .build();

    @PostConstruct
    public void loadExceptionMappings() {
        try {
            List<Error> list = errorDataRepository.findAll();
            list.forEach(error -> exceptionCache.put(error.getExceptionName(), error));
        } catch (Exception e) {
            log.warn("Could not preload exception cache: {}", e.getMessage());
        }
    }

    private String getErrorCode(String exceptionName) {
        Error error = exceptionCache.getIfPresent(exceptionName);
        if (error == null) {
            error = errorDataRepository.findByExceptionName(exceptionName).orElse(null);
            if (error != null) {
                exceptionCache.put(exceptionName, error);
            }
        }

        if (error != null && error.getErrorCode() != null) {
            return error.getErrorCode();
        }

        Error fallbackError = exceptionCache.getIfPresent("Exception");
        if (fallbackError != null && fallbackError.getErrorCode() != null) {
            return fallbackError.getErrorCode();
        }

        return "ERR_" + exceptionName.toUpperCase();
    }

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

    // 1. ResourceNotFoundException (ERR_001 - 404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("ResourceNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 2. IllegalArgumentException (ERR_002 - 400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 3. IllegalStateException (ERR_003 - 400)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("IllegalStateException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 4. MethodArgumentNotValidException (ERR_004 - 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("MethodArgumentNotValidException: {}", fieldErrors);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(false)
                .errorCode(errorCode)
                .message(errorMessage != null && !errorMessage.isBlank() ? errorMessage : "Input validation failed")
                .errorStatusCode(HttpStatus.BAD_REQUEST.value())
                .validationErrors(fieldErrors)
                .timestamp(Instant.now())
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmailNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailNotFoundException(EmailNotFoundException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("EmailNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.error("DataIntegrityViolationException: ", ex);
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        String message = ex.getRootCause() != null ? ex.getRootCause().getMessage() : ex.getMessage();
        return buildErrorResponse(errorCode, message, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponse<Void>> handleNullPointerException(NullPointerException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("NullPointerException occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Internal Server Error: Null reference encountered";
        return buildErrorResponse(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(GlobalException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("GlobalException occurred: {}", ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleExcelValidationException(ExcelValidationException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("ExcelValidationException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 11. ExcelProcessingException (ERR_011 - 400)
    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleExcelProcessingException(ExcelProcessingException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("ExcelProcessingException: {}", ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 12. MaxUploadSizeExceededException (ERR_012 - 413)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("MaxUploadSizeExceededException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, "Maximum upload size exceeded. Please upload a smaller file.", HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // 13. ObjectOptimisticLockingFailureException (ERR_013 - 409)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingException(ObjectOptimisticLockingFailureException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("Optimistic locking conflict: {}", ex.getMessage());
        return buildErrorResponse(
                errorCode,
                "Resource was modified by another user. Please refresh and try again.",
                HttpStatus.CONFLICT);
    }

    // 14. DuplicateResourceException (ERR_014 - 409)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateResourceException(DuplicateResourceException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("DuplicateResourceException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.CONFLICT);
    }

    // 15. AttachmentProcessingException (ERR_015 - 400)
    @ExceptionHandler(AttachmentProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleAttachmentProcessingException(AttachmentProcessingException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("AttachmentProcessingException: {}", ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 16. FileProcessingException (ERR_016 - 400)
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileProcessingException(FileProcessingException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("FileProcessingException: {}", ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 17. AccessDeniedException (ERR_017 - 403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("Access denied: {}", ex.getMessage());
        return buildErrorResponse(errorCode, "Access Denied: You do not have permission to access this resource.", HttpStatus.FORBIDDEN);
    }

    // 18. BadCredentialsException (ERR_018 - 401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildErrorResponse(errorCode, "Invalid username or password.", HttpStatus.UNAUTHORIZED);
    }

    // 19. AuthenticationException (ERR_019 - 401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("Authentication error: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 20. ExpiredJwtException (ERR_020 - 401)
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ApiResponse<Void>> handleExpiredJwtException(ExpiredJwtException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("JWT token expired: {}", ex.getMessage());
        return buildErrorResponse(errorCode, "Session has expired. Please login again.", HttpStatus.UNAUTHORIZED);
    }

    // 21. EmailSendingException (ERR_021 - 500)
    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailSendingException(EmailSendingException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("EmailSendingException: {}", ex.getMessage(), ex);
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 22. AccountDisabledException (ERR_022 - 403)
    @ExceptionHandler(AccountDisabledException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccountDisabledException(AccountDisabledException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("AccountDisabledException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // 23. InvalidTokenException (ERR_023 - 401)
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTokenException(InvalidTokenException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("InvalidTokenException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // UsernameNotFoundException (ERR_001 / ERR_005 - 404)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("UsernameNotFoundException: {}", ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Parent Business Exception Fallback (TestingAutomationException)
    @ExceptionHandler(TestingAutomationException.class)
    public ResponseEntity<ApiResponse<Void>> handleTestingAutomationException(TestingAutomationException ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.warn("TestingAutomationException ({}): {}", exceptionName, ex.getMessage());
        return buildErrorResponse(errorCode, ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // 9. Exception (ERR_999 - 500) - General Fallback Handler
    @ExceptionHandler(java.lang.Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(java.lang.Exception ex) {
        String exceptionName = ex.getClass().getSimpleName();
        String errorCode = getErrorCode(exceptionName);
        log.error("Unhandled exception occurred: ", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : "Internal Server Error";
        return buildErrorResponse(errorCode, message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}