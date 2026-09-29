package xyz.mobi.testingautomationtool.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Instant;

public record ErrorResponse(
        String errorCode,
        String errorMessage,
        Integer errorStatusCode,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        Instant time) {

    public ErrorResponse(
            String errorCode,
            String errorMessage,
            Integer errorStatusCode,
            Instant time) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorStatusCode = errorStatusCode;
        this.time = time;
    }
}
