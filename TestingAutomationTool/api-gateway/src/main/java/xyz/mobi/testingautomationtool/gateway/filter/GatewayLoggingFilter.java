package xyz.mobi.testingautomationtool.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Global filter for auditing request/response lifecycle through the API Gateway,
 * including monitoring of HttpOnly cookie authentication and response status codes.
 */
@Slf4j
@Component
public class GatewayLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        boolean hasAuthCookie = request.getCookies().containsKey("accessToken") || request.getCookies().containsKey("jwt");
        boolean hasAuthHeader = request.getHeaders().getFirst(org.springframework.http.HttpHeaders.AUTHORIZATION) != null;

        log.info("--> [GATEWAY IN] {} {} | AuthCookie: {}, AuthHeader: {}, Remote: {}",
                request.getMethod(),
                request.getURI().getPath(),
                hasAuthCookie,
                hasAuthHeader,
                request.getRemoteAddress());

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = System.currentTimeMillis() - startTime;
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            log.info("<-- [GATEWAY OUT] {} {} | Status: {} | Duration: {}ms",
                    request.getMethod(),
                    request.getURI().getPath(),
                    status != null ? status.value() : "UNKNOWN",
                    duration);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
