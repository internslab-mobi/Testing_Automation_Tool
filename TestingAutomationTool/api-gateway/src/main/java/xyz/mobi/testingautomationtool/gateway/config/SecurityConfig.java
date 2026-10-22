package xyz.mobi.testingautomationtool.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

/**
 * Centralized Reactive Security & CSRF Configuration for Spring Cloud Gateway.
 * Manages Double-Submit CSRF cookie validation for all downstream microservices.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // Safe methods (GET, HEAD, OPTIONS) and public auth endpoints are exempt from CSRF
        ServerWebExchangeMatcher csrfProtectionMatcher = exchange -> {
            HttpMethod method = exchange.getRequest().getMethod();
            if (method == HttpMethod.GET || method == HttpMethod.HEAD || method == HttpMethod.TRACE || method == HttpMethod.OPTIONS) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            String path = exchange.getRequest().getPath().value();
            if (path.startsWith("/auth/login") || path.startsWith("/auth/register") || path.startsWith("/auth/refresh")
                    || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/webjars")) {
                return ServerWebExchangeMatcher.MatchResult.notMatch();
            }
            return ServerWebExchangeMatcher.MatchResult.match();
        };

        CookieServerCsrfTokenRepository tokenRepository = CookieServerCsrfTokenRepository.withHttpOnlyFalse();

        return http
                // CSRF Protection via HttpOnly=false Cookie for SPA Double Submit pattern
                .csrf(csrf -> csrf
                        .csrfTokenRepository(tokenRepository)
                        .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(csrfProtectionMatcher)
                )
                // Downstream microservices authenticate JWT tokens and authorization
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    /**
     * WebFilter to ensure deferred CSRF token is resolved and Set-Cookie is written to response.
     */
    @Bean
    public WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
            if (csrfToken != null) {
                return csrfToken.doOnSuccess(token -> {}).then(chain.filter(exchange));
            }
            return chain.filter(exchange);
        };
    }
}
