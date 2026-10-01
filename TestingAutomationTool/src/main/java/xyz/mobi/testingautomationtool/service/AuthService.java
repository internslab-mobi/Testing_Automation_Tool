package xyz.mobi.testingautomationtool.service;

import xyz.mobi.testingautomationtool.dto.AuthDto.*;
import xyz.mobi.testingautomationtool.entity.User;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserProfileResponse getCurrentUserProfile(String username);
    void changePassword(String username, ChangePasswordRequest request);
    User getCurrentUser();
}
