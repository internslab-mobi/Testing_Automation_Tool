package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.AuthDTO.*;
import xyz.mobi.testingautomationtool.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    AuthResponse sendPasswordResetOtp(ForgotPasswordRequest request);
    AuthResponse verifyOtp(VerifyOtpRequest request);
    AuthResponse resetPassword(ResetPasswordRequest request);
//    AuthResponse resetPasswordWithOtp(ResetPasswordWithOtpRequest request);
    AuthResponse logout(LogoutRequest request);
    UserProfileResponse getCurrentUserProfile(String username);
    void changePassword(String username, ChangePasswordRequest request);
    User getCurrentUser();
}
