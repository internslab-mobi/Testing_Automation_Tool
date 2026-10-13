package xyz.mobi.testingautomationtool.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * Utility for creating and extracting secure HttpOnly cookies for JWT authentication.
 * 
 * Storing tokens in HttpOnly cookies ensures that client-side JavaScript (frontend)
 * cannot read, access, or exfiltrate JWT credentials via XSS attacks, while enabling
 * automatic browser credential propagation to the API Gateway and microservices.
 */
@Slf4j
public final class CookieUtils {

    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    // Fallback cookie aliases for broad compatibility
    public static final String JWT_COOKIE_NAME = "jwt";

    private CookieUtils() {
        // Prevent instantiation
    }

    /**
     * Builds an HttpOnly, secure ResponseCookie for the 15-minute Access Token.
     *
     * @param token         JWT Access Token
     * @param maxAgeSeconds Token lifespan in seconds (e.g. 900 for 15 minutes)
     * @param secure        Whether to require HTTPS (true in production, false for local dev)
     * @param sameSite      SameSite attribute (e.g. "Lax", "Strict", "None")
     * @return Prepared ResponseCookie
     */
    public static ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds, boolean secure, String sameSite) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite(StringUtils.hasText(sameSite) ? sameSite : "Lax")
                .build();
    }

    /**
     * Overload with default SameSite="Lax".
     */
    public static ResponseCookie createAccessTokenCookie(String token, long maxAgeSeconds, boolean secure) {
        return createAccessTokenCookie(token, maxAgeSeconds, secure, "Lax");
    }

    /**
     * Builds an HttpOnly, secure ResponseCookie for the 24-hour Refresh Token.
     *
     * @param refreshToken    JWT Refresh Token
     * @param maxAgeSeconds   Lifespan in seconds (e.g. 86400 for 24 hours)
     * @param secure          Whether to require HTTPS
     * @param sameSite        SameSite policy
     * @param path            Cookie path (default "/" or "/auth")
     * @return Prepared ResponseCookie
     */
    public static ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds, boolean secure, String sameSite, String path) {
        String cookiePath = StringUtils.hasText(path) ? path : "/";
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .sameSite(StringUtils.hasText(sameSite) ? sameSite : "Lax")
                .build();
    }

    /**
     * Overload with default path "/" and SameSite="Lax".
     */
    public static ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds, boolean secure) {
        return createRefreshTokenCookie(refreshToken, maxAgeSeconds, secure, "Lax", "/");
    }

    /**
     * Clears the Access Token cookie by setting maxAge to 0.
     */
    public static ResponseCookie deleteAccessTokenCookie(boolean secure, String sameSite) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(0)
                .sameSite(StringUtils.hasText(sameSite) ? sameSite : "Lax")
                .build();
    }

    public static ResponseCookie deleteAccessTokenCookie(boolean secure) {
        return deleteAccessTokenCookie(secure, "Lax");
    }

    /**
     * Clears the Refresh Token cookie by setting maxAge to 0.
     */
    public static ResponseCookie deleteRefreshTokenCookie(boolean secure, String sameSite, String path) {
        String cookiePath = StringUtils.hasText(path) ? path : "/";
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path(cookiePath)
                .maxAge(0)
                .sameSite(StringUtils.hasText(sameSite) ? sameSite : "Lax")
                .build();
    }

    public static ResponseCookie deleteRefreshTokenCookie(boolean secure) {
        return deleteRefreshTokenCookie(secure, "Lax", "/");
    }

    /**
     * Extracts a named cookie value from an incoming HttpServletRequest.
     */
    public static Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
        if (request == null || request.getCookies() == null || !StringUtils.hasText(cookieName)) {
            return Optional.empty();
        }

        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equalsIgnoreCase(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    /**
     * Extracts the Access Token from either "accessToken" or "jwt" cookies.
     */
    public static Optional<String> getAccessTokenFromCookies(HttpServletRequest request) {
        Optional<String> token = getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
        if (token.isPresent()) {
            return token;
        }
        return getCookieValue(request, JWT_COOKIE_NAME);
    }

    /**
     * Extracts the Refresh Token from the "refreshToken" cookie.
     */
    public static Optional<String> getRefreshTokenFromCookies(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);
    }
}
