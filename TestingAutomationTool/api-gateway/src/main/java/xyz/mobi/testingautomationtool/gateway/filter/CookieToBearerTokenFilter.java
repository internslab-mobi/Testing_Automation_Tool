package xyz.mobi.testingautomationtool.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global Gateway filter that extracts JWT access tokens from HttpOnly cookies and relays
 * them as standard "Authorization: Bearer <token>" headers downstream to microservices.
 *
 * This allows frontend browser JavaScript to avoid holding or accessing raw JWT tokens
 * directly (preventing XSS credential leakage), while microservices receive seamless
 * Bearer token authentication headers.
 */
@Slf4j
@Component
public class CookieToBearerTokenFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.cookie.name:accessToken}")
    private String cookieName;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // If client already supplied an Authorization header, respect it
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            log.debug("Gateway found existing Authorization header for path: {}", request.getPath());
            return chain.filter(exchange);
        }

        // Look for HttpOnly access token cookie
        String tokenFromCookie = extractTokenFromCookies(request);

        if (StringUtils.hasText(tokenFromCookie)) {
            log.debug("Gateway extracted HttpOnly {} cookie for path: {}, injecting Bearer header downstream",
                    cookieName, request.getPath());

            ServerHttpRequest mutatedRequest = request.mutate()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenFromCookie)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        }

        return chain.filter(exchange);
    }

    private String extractTokenFromCookies(ServerHttpRequest request) {
        HttpCookie primaryCookie = request.getCookies().getFirst(cookieName);
        if (primaryCookie != null && StringUtils.hasText(primaryCookie.getValue())) {
            return primaryCookie.getValue();
        }

        // Fallback to "jwt" cookie name
        HttpCookie fallbackCookie = request.getCookies().getFirst("jwt");
        if (fallbackCookie != null && StringUtils.hasText(fallbackCookie.getValue())) {
            return fallbackCookie.getValue();
        }

        return null;
    }

    @Override
    public int getOrder() {
        // High priority: run before routing and before validation filter
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
