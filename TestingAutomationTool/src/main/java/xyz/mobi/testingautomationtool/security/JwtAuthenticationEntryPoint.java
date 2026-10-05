package xyz.mobi.testingautomationtool.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.exception.ErrorResponse;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        log.error("Unauthorized error: {}", authException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Boolean isExpired = (Boolean) request.getAttribute("jwt_expired");
        String customErrorMessage = (String) request.getAttribute("jwt_error_message");

        String errorMessage;
        String errorCode;

        if (Boolean.TRUE.equals(isExpired)) {
            errorCode = "ERR_TOKEN_EXPIRED";
            errorMessage = customErrorMessage != null
                    ? customErrorMessage
                    : "Access token has expired (validity: 15 minutes). Please use /auth/refresh with your 24-hour refresh token to obtain a new access token.";
        } else if (customErrorMessage != null) {
            errorCode = "ERR_INVALID_TOKEN";
            errorMessage = customErrorMessage;
        } else {
            errorCode = "ERR_020";
            errorMessage = "Unauthorized access: Full authentication is required to access this resource";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .errorStatusCode(HttpServletResponse.SC_UNAUTHORIZED)
                .time(Instant.now())
                .build();

        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
