package xyz.mobi.testingautomationtool.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.exception.ErrorResponse;

import java.io.IOException;
import java.time.Instant;

@Slf4j
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<Void> apiResponse = ApiResponse.error(
                "ER_0021",
                "Access denied: You do not have sufficient permissions to perform this action.",
                HttpServletResponse.SC_FORBIDDEN
        );

        objectMapper.writeValue(
                response.getOutputStream(),
                apiResponse
        );
    }
}
