package xyz.mobi.testingautomationtool.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;

/**
 * Validates JWT access tokens at the API Gateway level before requests reach downstream services.
 * 
 * Verifies cryptographic integrity, expiration, and extracts claims (username, role, userId)
 * to propagate downstream in X-Auth headers.
 */
@Slf4j
@Component
public class GatewayJwtValidatorFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret:404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970}")
    private String jwtSecret;

    // Public URI prefixes that bypass gateway JWT authentication
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/auth/",
            "/gateway/",
            "/static/",
            "/v3/api-docs",
            "/swagger-ui",
            "/bug-chat.html",
            "/manager-dashboard.html",
            "/favicon.ico"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Allow CORS pre-flight OPTIONS requests
        if (HttpMethod.OPTIONS.equals(request.getMethod())) {
            return chain.filter(exchange);
        }

        // Allow public paths
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // For protected endpoints, extract the Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("Unauthorized request to protected path: {} (no token found in cookie or header)", path);
            return onError(exchange, HttpStatus.UNAUTHORIZED,
                    "Authentication required. No valid HttpOnly accessToken cookie or Bearer token provided.");
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = getSigningKey();
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenType = claims.get("tokenType", String.class);
            if (tokenType != null && !"ACCESS".equalsIgnoreCase(tokenType)) {
                log.warn("Attempt to access API with invalid token type: {} on path: {}", tokenType, path);
                return onError(exchange, HttpStatus.FORBIDDEN,
                        "Refresh token cannot be used to access APIs directly. Please use /auth/refresh.");
            }

            // Propagate authenticated identity downstream
            String username = claims.getSubject();
            Object role = claims.get("role");
            Object userId = claims.get("userId");

            ServerHttpRequest.Builder builder = request.mutate();
            if (StringUtils.hasText(username)) {
                builder.header("X-Auth-Username", username);
            }
            if (role != null) {
                builder.header("X-Auth-Role", role.toString());
            }
            if (userId != null) {
                builder.header("X-Auth-UserId", userId.toString());
            }

            return chain.filter(exchange.mutate().request(builder.build()).build());

        } catch (ExpiredJwtException e) {
            log.warn("Access token expired for path: {}. Expiration: {}", path, e.getClaims().getExpiration());
            return onError(exchange, HttpStatus.UNAUTHORIZED,
                    "Access token has expired (15-min validity). Please refresh your session via /auth/refresh.");
        } catch (JwtException e) {
            log.warn("Invalid JWT token for path: {}: {}", path, e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED,
                    "Invalid or tampered authentication token: " + e.getMessage());
        } catch (Exception e) {
            log.error("Authentication error for path: {}: {}", path, e.getMessage());
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Authentication failure: " + e.getMessage());
        }
    }

    private boolean isPublicPath(String path) {
        if ("/".equals(path)) {
            return true;
        }
        for (String prefix : PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix) || path.equals(prefix)) {
                return true;
            }
        }
        return false;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtSecret);
        } catch (Exception e) {
            keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                keyBytes = md.digest(keyBytes);
            } catch (Exception ignored) {}
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String json = String.format(
                "{\"success\":false,\"message\":\"%s\",\"statusCode\":%d,\"timestamp\":\"%s\"}",
                escapeJson(errorMessage),
                status.value(),
                Instant.now().toString()
        );

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    private String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Override
    public int getOrder() {
        // Runs immediately after CookieToBearerTokenFilter
        return Ordered.HIGHEST_PRECEDENCE + 20;
    }
}
