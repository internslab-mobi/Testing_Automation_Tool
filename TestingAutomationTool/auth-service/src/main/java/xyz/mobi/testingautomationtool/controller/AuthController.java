package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.ApiResponse;
import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.security.CookieUtils;
import xyz.mobi.testingautomationtool.security.JwtUtils;
import xyz.mobi.testingautomationtool.service.AuthService;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and User Management Endpoints with HttpOnly Cookie Security")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Value("${app.jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.jwt.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Operation(summary = "Initialize CSRF cookie token", description = "Generates and sends the XSRF-TOKEN cookie for the frontend client.")
    @GetMapping("/csrf")
    public ResponseEntity<ApiResponse<String>> getCsrfToken(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token == null) {
            token = (CsrfToken) request.getAttribute("_csrf");
        }
        String tokenValue = token != null ? token.getToken() : "OK";
        return ResponseEntity.ok(ApiResponse.success("CSRF token initialized", tokenValue));
    }

    @Operation(summary = "Register a new user", description = "Allows user registration with specific roles: MANAGER, TESTER, DEVELOPER, ADMIN")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @Operation(
            summary = "Authenticate user and set HttpOnly security cookies",
            description = "Log in using username or email with password. Issues HttpOnly cookies (accessToken and refreshToken) to prevent frontend JavaScript from accessing token data, preventing XSS credential theft."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.login(request);
        setAuthCookies(httpResponse, response.getToken(), response.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @Operation(
            summary = "Refresh access token using 24-hour refresh token or HttpOnly cookie",
            description = "Exchange a valid 24-hour refresh token for a new 15-minute access token. Can be called with empty body if HttpOnly refreshToken cookie is present."
    )
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String tokenToRefresh = (request != null && StringUtils.hasText(request.getRefreshToken()))
                ? request.getRefreshToken()
                : CookieUtils.getRefreshTokenFromCookies(httpRequest).orElse(null);

        if (!StringUtils.hasText(tokenToRefresh)) {
            throw new BadCredentialsException("Refresh token is missing. Please provide it in request body or via HttpOnly refreshToken cookie.");
        }

        RefreshTokenRequest resolvedRequest = RefreshTokenRequest.builder()
                .refreshToken(tokenToRefresh)
                .build();

        AuthResponse response = authService.refreshToken(resolvedRequest);
        setAuthCookies(httpResponse, response.getToken(), response.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @Operation(summary = "Send password reset OTP to email", description = "Generates and emails a 6-digit OTP valid for 10 minutes to the user's registered email.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<AuthResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthResponse response = authService.sendPasswordResetOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset OTP sent to email", response));
    }

    @Operation(
            summary = "Verify email OTP and generate Reset Token",
            description = "Validates the 6-digit OTP code sent to the email. If valid, generates a one-time Reset Token valid for 15 minutes to be used in /auth/reset-password."
    )
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully", response));
    }

    @Operation(
            summary = "Reset password using Reset Token, auto-login and set HttpOnly cookies",
            description = "Validates the temporary Reset Token, updates user password, revokes previous sessions, and sets HttpOnly accessToken and refreshToken cookies."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletResponse httpResponse
    ) {
        AuthResponse response = authService.resetPassword(request);
        setAuthCookies(httpResponse, response.getToken(), response.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", response));
    }

    @Operation(
            summary = "Logout, revoke refresh token and clear HttpOnly security cookies",
            description = "Revokes the active refresh token in the database and clears the HttpOnly cookies from the browser."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<AuthResponse>> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        String tokenToRevoke = (request != null && StringUtils.hasText(request.getRefreshToken()))
                ? request.getRefreshToken()
                : CookieUtils.getRefreshTokenFromCookies(httpRequest).orElse(null);

        LogoutRequest resolvedRequest = LogoutRequest.builder()
                .refreshToken(tokenToRevoke)
                .build();

        AuthResponse response = authService.logout(resolvedRequest);
        clearAuthCookies(httpResponse);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", response));
    }

    @Operation(summary = "Get current authenticated user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = authService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved successfully", profile));
    }

    @Operation(summary = "Change password for logged in user", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Helper to set HttpOnly, secure cookies for Access and Refresh tokens.
     */
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        if (StringUtils.hasText(accessToken)) {
            ResponseCookie accessCookie = CookieUtils.createAccessTokenCookie(
                    accessToken,
                    jwtUtils.getAccessTokenExpirationMs() / 1000,
                    cookieSecure,
                    cookieSameSite
            );
            response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        }
        if (StringUtils.hasText(refreshToken)) {
            ResponseCookie refreshCookie = CookieUtils.createRefreshTokenCookie(
                    refreshToken,
                    jwtUtils.getRefreshTokenExpirationMs() / 1000,
                    cookieSecure,
                    cookieSameSite,
                    "/"
            );
            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }
    }

    /**
     * Helper to clear HttpOnly cookies on logout.
     */
    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie deleteAccess = CookieUtils.deleteAccessTokenCookie(cookieSecure, cookieSameSite);
        ResponseCookie deleteRefresh = CookieUtils.deleteRefreshTokenCookie(cookieSecure, cookieSameSite, "/");
        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefresh.toString());
    }
}
