package xyz.mobi.testingautomationtool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and User Management Endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Allows user registration with specific roles: MANAGER, TESTER, DEVELOPER, ADMIN")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Authenticate user and get JWT token", description = "Log in using username or email with password")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using 24-hour refresh token", description = "Exchange a valid 24-hour refresh token for a new 15-minute access token. If refresh token is expired, returns session expired message.")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send password reset OTP to email", description = "Generates and emails a 6-digit OTP valid for 10 minutes to the user's registered email.")
    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthResponse response = authService.sendPasswordResetOtp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify email OTP", description = "Validates if the submitted 6-digit OTP code is correct and not expired.")
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reset password with email OTP and auto-login", description = "Validates the OTP, updates the user's password, revokes previous sessions, and immediately returns 15m access token & 24h refresh token.")
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordWithOtpRequest request) {
        AuthResponse response = authService.resetPasswordWithOtp(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Logout and revoke refresh token", description = "Revokes the active refresh token in the database to terminate the session.")
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@Valid @RequestBody LogoutRequest request) {
        AuthResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current authenticated user profile", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentProfile(@AuthenticationPrincipal UserDetails userDetails) {
        UserProfileResponse profile = authService.getCurrentUserProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Change password for logged in user", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
