package xyz.mobi.testingautomationtool.exception;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.testingautomationtool.dto.response.ErrorResponse;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.time.LocalDateTime;
import java.util.List;
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
        List<Error> list = errorDataRepository.findAll();
        list.forEach(error -> exceptionCache.put(error.getExceptionName(),error));
    }

    private Error getErrorMapping(String exceptionName) {
        Error error  =  exceptionCache.getIfPresent(exceptionName);
        if (error == null) {
            return exceptionCache.getIfPresent("Exception");
        }
        return error;
    }

    private String getErrorCode(String exceptionName) {
        Error error = getErrorMapping(exceptionName);
        if (error != null && error.getErrorCode() != null) {
            return error.getErrorCode();
        }
        return "ERR_" + exceptionName.toUpperCase();
    }



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

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponse> handleFileProcessingError(FileProcessingException exception){
        String exceptionName = exception.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);

        return buildErrorResponse(mapping,exception.getMessage(),HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockingException(
            ObjectOptimisticLockingFailureException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "Optimistic locking conflict: {}",
                ex.getMessage());

        return buildErrorResponse(
                mapping,
                "Test case was modified by another user. Please refresh and try again.",
                HttpStatus.CONFLICT);
    }


    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.error(
                "NullPointerException occurred: ",
                ex);

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Internal Server Error";

        return buildErrorResponse(
                mapping,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "ResourceNotFoundException: {}",
                ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "IllegalArgumentException: {}",
                ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "IllegalStateException: {}",
                ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errorMessage =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.joining(", "));

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "Validation failed: {}",
                errorMessage);

        return buildErrorResponse(
                mapping,
                errorMessage,
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException ex) {

        log.error(
                "DataIntegrityViolationException: ",
                ex);

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        String message =
                ex.getRootCause() != null
                        ? ex.getRootCause().getMessage()
                        : ex.getMessage();

        return buildErrorResponse(
                mapping,
                message,
                HttpStatus.CONFLICT);
    }


    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            GlobalException ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.warn(
                "GlobalException: {}",
                ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            org.springframework.security.authentication.BadCredentialsException ex) {

        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);

        log.warn("Bad credentials: {}", ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            org.springframework.security.core.userdetails.UsernameNotFoundException ex) {

        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);

        log.warn("User not found: {}", ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex) {

        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);

        log.warn("Access denied: {}", ex.getMessage());

        return buildErrorResponse(
                mapping,
                "Access Denied: You do not have permission to access this resource. Only MANAGER and TESTER roles can access these methods.",
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            org.springframework.security.core.AuthenticationException ex) {

        String exceptionName = ex.getClass().getSimpleName();
        String mapping = getErrorCode(exceptionName);

        log.warn("Authentication error: {}", ex.getMessage());

        return buildErrorResponse(
                mapping,
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex) {

        String exceptionName =
                ex.getClass().getSimpleName();

        String mapping =
                getErrorCode(exceptionName);

        log.error(
                "Unhandled exception occurred: ",
                ex);

        String message =
                ex.getMessage() != null
                        ? ex.getMessage()
                        : "Internal Server Error";

        return buildErrorResponse(
                mapping,
                message,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }
}