package xyz.mobi.testingautomationtool.exception;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record ErrorResponse(
        String errorCode,
        String errorMessage,
        Integer errorStatusCode,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime time) {

    public ErrorResponse(
            String errorCode,
            String errorMessage,
            Integer errorStatusCode,
            LocalDateTime time) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorStatusCode = errorStatusCode;
        this.time = time;
    }

}
