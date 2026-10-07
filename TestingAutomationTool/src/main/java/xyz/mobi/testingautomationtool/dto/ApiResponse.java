package xyz.mobi.testingautomationtool.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    private String errorCode;

    private Integer errorStatusCode;

    private Map<String, String> validationErrors;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    @Builder.Default
    private Instant timestamp = Instant.now();

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Operation successful", data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message, Integer errorStatusCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .errorStatusCode(errorStatusCode)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return error(errorCode, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(null, message, null);
    }

    public static <T> ApiResponse<T> validationError(String errorCode, String message, Integer errorStatusCode, Map<String, String> validationErrors) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .errorStatusCode(errorStatusCode)
                .validationErrors(validationErrors)
                .timestamp(Instant.now())
                .build();
    }
}
