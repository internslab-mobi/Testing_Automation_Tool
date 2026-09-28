package xyz.mobi.testingautomationtool.exception;

import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalException {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(
            CustomException exception, HttpServletRequest request) {
        return buildResponse(exception.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.VALIDATION_ERROR);
    }

    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            Exception exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            Exception exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.DATA_INTEGRITY_VIOLATION);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request) {
        exception.printStackTrace();
        return buildResponse(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(ErrorCode errorCode) {
        ErrorResponse response = new ErrorResponse(
                errorCode.getInternalErrorCode(),
                errorCode.getResponseMessage(),
                errorCode.getResponseCode().value(),
                LocalDateTime.now());
        return ResponseEntity.status(errorCode.getResponseCode()).body(response);
    }
}
