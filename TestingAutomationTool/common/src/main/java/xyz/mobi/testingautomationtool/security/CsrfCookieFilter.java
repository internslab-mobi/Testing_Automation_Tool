package xyz.mobi.testingautomationtool.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that forces resolution of Spring Security's DeferredCsrfToken on every request.
 *
 * In Spring Security 6/7, CSRF tokens are loaded lazily. This filter ensures that the
 * XSRF-TOKEN cookie is generated and sent in the response so single-page applications
 * and frontend clients can read it and send it back in the X-XSRF-TOKEN request header.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken != null) {
            // Calling getToken() triggers the deferred token generation and writes the cookie
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}
