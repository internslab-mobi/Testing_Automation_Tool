package xyz.mobi.testingautomationtool.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Endpoint to serve CSRF token initialization directly at the API Gateway level.
 * Ensures the XSRF-TOKEN cookie is set and returns matching JSON for frontend clients.
 */
@RestController
public class CsrfController {

    @GetMapping("/csrf")
    public Mono<ResponseEntity<Map<String, Object>>> getCsrfToken(ServerWebExchange exchange) {
        Mono<CsrfToken> tokenMono = exchange.getAttribute(CsrfToken.class.getName());
        if (tokenMono == null) {
            tokenMono = exchange.getAttribute("_csrf");
        }

        if (tokenMono == null) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("success", true);
            fallback.put("message", "CSRF token initialized");
            fallback.put("data", "OK");
            fallback.put("timestamp", LocalDateTime.now().toString());
            return Mono.just(ResponseEntity.ok(fallback));
        }

        return tokenMono.map(token -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CSRF token initialized");
            response.put("data", token.getToken());
            response.put("timestamp", LocalDateTime.now().toString());
            return ResponseEntity.ok(response);
        });
    }
}
