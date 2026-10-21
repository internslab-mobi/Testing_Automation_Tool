package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.mobi.testingautomationtool.dto.ApiResponse;

/**
 * Endpoint to explicitly trigger deferred CSRF token resolution in main-service
 * and return the active CSRF token value while writing the XSRF-TOKEN cookie.
 */
@RestController
@RequestMapping("/csrf")
@Tag(name = "CSRF", description = "CSRF Token Initializer")
public class CsrfController {

    @Operation(summary = "Initialize CSRF cookie token", description = "Generates and sends the XSRF-TOKEN cookie for the frontend client.")
    @GetMapping
    public ResponseEntity<ApiResponse<String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        String tokenValue = token != null ? token.getToken() : "OK";
        return ResponseEntity.ok(ApiResponse.success("CSRF token initialized", tokenValue));
    }
}
