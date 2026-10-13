package xyz.mobi.testingautomationtool.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health & diagnostics controller for the API Gateway service.
 */
@RestController
@RequestMapping("/gateway")
public class GatewayHealthController {

    @Value("${services.auth-service.url:http://localhost:8081}")
    private String authServiceUrl;

    @Value("${services.main-service.url:http://localhost:8090}")
    private String mainServiceUrl;

    @Value("${app.jwt.cookie.name:accessToken}")
    private String cookieName;

    @Value("${app.jwt.cookie.refresh-name:refreshToken}")
    private String refreshCookieName;

    @Value("${app.jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie.same-site:Lax}")
    private String cookieSameSite;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getGatewayHealth() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "api-gateway");
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());

        Map<String, String> routes = new LinkedHashMap<>();
        routes.put("/auth/**", authServiceUrl);
        routes.put("/manager/**", mainServiceUrl);
        routes.put("/project/**", mainServiceUrl);
        routes.put("/feature/**", mainServiceUrl);
        routes.put("/testcases/**", mainServiceUrl);
        routes.put("/bugs/**", mainServiceUrl);
        routes.put("/comments/**", mainServiceUrl);
        routes.put("/attachments/**", mainServiceUrl);
        routes.put("/in-app-notifications/**", mainServiceUrl);
        routes.put("/admin/**", mainServiceUrl);
        routes.put("/static/**", mainServiceUrl);
        response.put("downstreamRoutes", routes);

        Map<String, Object> cookieSecurity = new LinkedHashMap<>();
        cookieSecurity.put("enabled", true);
        cookieSecurity.put("accessTokenCookie", cookieName);
        cookieSecurity.put("refreshTokenCookie", refreshCookieName);
        cookieSecurity.put("httpOnly", true);
        cookieSecurity.put("secure", cookieSecure);
        cookieSecurity.put("sameSite", cookieSameSite);
        cookieSecurity.put("frontendProtection", "Tokens are stored in browser HttpOnly cookies; client-side JavaScript cannot access token credentials via document.cookie or localStorage, preventing XSS token harvesting.");
        cookieSecurity.put("cookieToBearerRelay", "Active - Gateway automatically injects Authorization Bearer header downstream if cookie is present.");
        response.put("cookieSecurityPolicy", cookieSecurity);

        return ResponseEntity.ok(response);
    }
}
