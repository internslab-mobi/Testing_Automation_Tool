package xyz.mobi.testingautomationtool.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

/**
 * Custom CSRF Token Request Handler for Single Page Applications (SPAs).
 *
 * In Spring Security 6/7, XorCsrfTokenRequestAttributeHandler applies XOR encoding to
 * mitigate BREACH attacks. For SPAs sending the token in an HTTP header (e.g. X-XSRF-TOKEN),
 * this handler delegates to XorCsrfTokenRequestAttributeHandler when rendering, but resolves
 * the raw token from the header without requiring client-side XOR reversal.
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestHandler delegate = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.delegate.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        final boolean hasHeader = StringUtils.hasText(request.getHeader(csrfToken.getHeaderName()));
        return hasHeader ? super.resolveCsrfTokenValue(request, csrfToken) : this.delegate.resolveCsrfTokenValue(request, csrfToken);
    }
}
