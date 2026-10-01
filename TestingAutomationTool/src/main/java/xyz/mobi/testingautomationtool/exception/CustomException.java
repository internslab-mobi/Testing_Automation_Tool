package xyz.mobi.testingautomationtool.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getResponseMessage());
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getResponseMessage());
        this.errorCode = errorCode;
    }
}
