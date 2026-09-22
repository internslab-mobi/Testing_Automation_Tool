    package xyz.mobi.testingautomationtool.enums;

    import lombok.Getter;
    import lombok.RequiredArgsConstructor;

    @Getter
    @RequiredArgsConstructor
    public enum ErrorCode {

        NULL_POINTER_EXCEPTION("E000_1", "Null pointer encountered"),
        RESOURCE_NOT_FOUND("E000_2", "Requested resource not found"),
        INVALID_ARGUMENT("E000_3", "Invalid request argument"),
        VALIDATION_FAILED("E000_4", "Validation failed"),
        ILLEGAL_STATE("E000_5", "Illegal operation state"),
        DATABASE_ERROR("E000_6", "Database integrity violation"),
        INTERNAL_SERVER_ERROR("E000_999", "An unexpected internal server error occurred");

        private final String code;
        private final String defaultMessage;
    }
