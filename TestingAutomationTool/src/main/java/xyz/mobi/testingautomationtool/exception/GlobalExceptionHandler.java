package xyz.mobi.testingautomationtool.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.mobi.testingautomationtool.dto.response.ErrorResponse;
import xyz.mobi.testingautomationtool.entity.Error;
import xyz.mobi.testingautomationtool.repository.ErrorDataRepository;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ErrorDataRepository errorDataRepository;

    @ExceptionHandler(ExcelValidationException.class)
    public ResponseEntity<ErrorResponse> handleExcelValidationException(
            ExcelValidationException ex) {

        log.warn("Excel validation failed: {}", ex.getMessage());

        String errorCode = getErrorCode(ex);

        return buildErrorResponse(
                errorCode,
                ex.getMessage(),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<ErrorResponse> handleExcelProcessingException(
            ExcelProcessingException ex) {

        log.error("Excel processing failed", ex);

        String errorCode = getErrorCode(ex);

        return buildErrorResponse(
                errorCode,
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            String errorCode,
            String errorMessage,
            HttpStatus status) {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .errorStatus(status.value())
                .errorMessage(errorMessage)
                .errorTime(LocalDateTime.now())
                .build();

        return ResponseEntity
                .status(status)
                .body(errorResponse);
    }

    private String getErrorCode(Exception exception) {

        String abbreviation =
                exception.getClass().getSimpleName();

        return errorDataRepository
                .findByExceptionName(abbreviation)
                .map(Error::getErrorCode)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Error code not configured for: "
                                        + abbreviation
                        )
                );
    }
}
