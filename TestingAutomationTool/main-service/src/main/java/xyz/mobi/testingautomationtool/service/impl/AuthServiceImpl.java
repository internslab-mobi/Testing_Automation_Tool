package xyz.mobi.testingautomationtool.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.entity.User;
import xyz.mobi.testingautomationtool.security.CustomUserDetails;
import xyz.mobi.testingautomationtool.service.AuthService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userDetails.getUser();
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        throw new UnsupportedOperationException("Registration is handled by auth-service");
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        throw new UnsupportedOperationException("Login is handled by auth-service");
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        throw new UnsupportedOperationException("Token refresh is handled by auth-service");
    }

    @Override
    public AuthResponse sendPasswordResetOtp(ForgotPasswordRequest request) {
        throw new UnsupportedOperationException("Password reset is handled by auth-service");
    }

    @Override
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        throw new UnsupportedOperationException("OTP verification is handled by auth-service");
    }

    @Override
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        throw new UnsupportedOperationException("Password reset is handled by auth-service");
    }

    @Override
    public AuthResponse logout(LogoutRequest request) {
        throw new UnsupportedOperationException("Logout is handled by auth-service");
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(String username) {
        throw new UnsupportedOperationException("User profile is handled by auth-service");
    }

    @Override
    public void changePassword(String username, ChangePasswordRequest request) {
        throw new UnsupportedOperationException("Password change is handled by auth-service");
    }
}
