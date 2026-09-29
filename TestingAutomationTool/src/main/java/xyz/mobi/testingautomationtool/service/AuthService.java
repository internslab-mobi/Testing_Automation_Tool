package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.request.auth.ChangePasswordRequest;
import xyz.mobi.testingautomationtool.dto.request.auth.LoginRequest;
import xyz.mobi.testingautomationtool.dto.request.auth.RegisterRequest;
import xyz.mobi.testingautomationtool.dto.response.auth.AuthResponse;
import xyz.mobi.testingautomationtool.dto.response.auth.UserProfileResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileResponse getCurrentUserProfile(String username);
    void changePassword(String username, ChangePasswordRequest request);
    Integer getCurrentUserId();
}
